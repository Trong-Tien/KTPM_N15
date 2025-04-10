package iuh.fit.order_service.service;

import iuh.fit.order_service.client.RestaurantClient;
import iuh.fit.order_service.client.UserClient;
import iuh.fit.order_service.controller.OrderStatusStatsDTO;
import iuh.fit.order_service.dto.*;
import iuh.fit.order_service.exception.ResourceNotFoundException;
import iuh.fit.order_service.kafka.KafkaProducerService;
import iuh.fit.order_service.kafka.OrderCreatedEvent;
import iuh.fit.order_service.kafka.OrderStatusUpdateEvent;
import iuh.fit.order_service.mapper.OrderMapper;
import iuh.fit.order_service.model.*;
import iuh.fit.order_service.repository.CartItemRepository;
import iuh.fit.order_service.repository.OrderRepository;
import iuh.fit.order_service.repository.OrderHistoryRepository;
import iuh.fit.order_service.repository.OrderRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderHistoryRepository orderHistoryRepository;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private UserClient userClient;

    @Autowired
    private RestaurantClient restaurantClient;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepositoryCustom orderRepositoryCustom;

    @Autowired
    private MongoTemplate mongoTemplate;


    public OrderResponse createOrder(String userId, CreateOrderRequest request) {
        if (!userClient.checkUserExists(userId)) {
            throw new ResourceNotFoundException("Người dùng không tồn tại");
        }

        if (!restaurantClient.checkRestaurantExists(request.getRestaurantId())) {
            throw new ResourceNotFoundException("Nhà hàng không tồn tại");
        }

        if (request.getPaymentMethod().equalsIgnoreCase("ONLINE")) {
            if (!userClient.hasLinkedBankAccount(userId)) {
                throw new RuntimeException("Bạn chưa liên kết tài khoản ngân hàng. Vui lòng liên kết trước khi thanh toán online.");
            }
        }

        List<OrderItem> items = request.getItems().stream().map(itemDTO -> {
            if (!restaurantClient.checkMenuItemExists(itemDTO.getMenuItemId())) {
                throw new ResourceNotFoundException("Món ăn không tồn tại: " + itemDTO.getMenuItemId());
            }

            MenuItemDTO menuItem = restaurantClient.getMenuItemById(request.getRestaurantId(), itemDTO.getMenuItemId());

            double basePrice = menuItem.getPrice();
            double finalPrice = basePrice;

            try {
                PromotionDTO promotion = restaurantClient.getPromotion(request.getRestaurantId(), itemDTO.getMenuItemId());

                System.out.println("📦 Món ăn: " + menuItem.getName());
                System.out.println("🎯 Giá gốc: " + basePrice);
                System.out.println("📢 Promotion nhận được: " + promotion);

                if (promotion != null) {
                    LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
                    System.out.println("🕒 Hôm nay là: " + today);
                    System.out.println("⏳ Hiệu lực: " + promotion.getStartDate() + " đến " + promotion.getEndDate());

                    if ((today.isEqual(promotion.getStartDate()) || today.isAfter(promotion.getStartDate())) &&
                            (today.isEqual(promotion.getEndDate()) || today.isBefore(promotion.getEndDate()))) {

                        finalPrice = basePrice * (1 - promotion.getDiscountPercent() / 100.0);
                        System.out.println("✅ Áp dụng khuyến mãi: " + promotion.getDiscountPercent() + "% → Giá sau giảm: " + finalPrice);
                    } else {
                        System.out.println("❌ Không nằm trong khoảng khuyến mãi.");
                    }
                }
            } catch (Exception e) {
                System.out.println("⚠️ Lỗi khi gọi khuyến mãi: " + e.getMessage());
            }

            OrderItem item = new OrderItem();
            item.setMenuItemId(itemDTO.getMenuItemId());
            item.setQuantity(itemDTO.getQuantity());
            item.setPriceAtOrderTime(finalPrice);
            return item;
        }).collect(Collectors.toList());


        Order order = new Order();
        order.setUserId(userId);
        order.setRestaurantId(request.getRestaurantId());
        order.setItems(items);
        order.setStatus(OrderStatus.CREATED.name());
        order.setPaymentMethod(request.getPaymentMethod()); // ✨
        order.setPaymentStatus("UNPAID");                   // ✨ Mặc định chưa thanh toán

        double total = items.stream()
                .mapToDouble(i -> i.getPriceAtOrderTime() * i.getQuantity())
                .sum();
        order.setTotalPrice(total);

        RestaurantDTO restaurant = restaurantClient.getRestaurantById(request.getRestaurantId());
        double commissionRate = (restaurant.getCommissionRate() != null) ? restaurant.getCommissionRate() : 0.3;

        double platformProfit = total * commissionRate;
        double restaurantRevenue = total - platformProfit;

        order.setCommissionRate(commissionRate);
        order.setPlatformProfit(platformProfit);
        order.setRestaurantRevenue(restaurantRevenue);


        Order saved = orderRepository.save(order);

        // Ghi lịch sử trạng thái CREATED
        orderHistoryRepository.save(new OrderHistory(null, saved.getId(), OrderStatus.CREATED.name(), "Đơn hàng đã được tạo.", LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))));

        OrderCreatedEvent event = new OrderCreatedEvent();
        event.setOrderId(saved.getId());
        event.setRestaurantId(saved.getRestaurantId());
        event.setItems(saved.getItems().stream()
                .map(i -> new OrderCreatedEvent.OrderItemKafka(i.getMenuItemId(), i.getQuantity()))
                .toList());
        kafkaProducerService.sendOrderCreatedEvent(event);

        return OrderMapper.toResponse(saved);
    }

    public List<OrderResponse> getOrdersByUser(String userId) {
        return orderRepository.findByUserId(userId)
                .stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        return OrderMapper.toResponse(order);
    }

    public OrderResponse updateOrderStatus(String orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));

        order.setStatus(newStatus);
        orderRepository.save(order);

        orderHistoryRepository.save(new OrderHistory(null, order.getId(), newStatus, OrderMapper.getStatusDescription(newStatus), LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))));

        return OrderMapper.toResponse(order);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status)
                .stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    public boolean cancelOrder(String orderId, String userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));

        if (!order.getUserId().equals(userId)) {
            return false;
        }

        if (!order.getStatus().equals(OrderStatus.CREATED.name())) {
            return false;
        }

        order.setStatus(OrderStatus.CANCELED.name());
        orderRepository.save(order);

        orderHistoryRepository.save(new OrderHistory(null, orderId, OrderStatus.CANCELED.name(), "Đơn hàng đã bị huỷ.", LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))));

        System.out.println("🚫 Đơn hàng đã bị huỷ bởi người dùng: " + userId + ", ID đơn: " + orderId);

        List<OrderStatusUpdateEvent.OrderItemKafka> items = order.getItems().stream()
                .map(i -> new OrderStatusUpdateEvent.OrderItemKafka(i.getMenuItemId(), i.getQuantity()))
                .toList();

        OrderStatusUpdateEvent cancelEvent = new OrderStatusUpdateEvent(orderId, OrderStatus.CANCELED.name(), items);
        kafkaProducerService.sendOrderStatus(cancelEvent);

        return true;
    }

    public boolean hasUserOrderedAndReceived(String userId, String menuItemId) {
        return orderRepository.findByUserId(userId).stream()
                .filter(order -> order.getStatus().equals("DELIVERED"))
                .flatMap(order -> order.getItems().stream())
                .anyMatch(item -> item.getMenuItemId().equals(menuItemId));
    }

    public boolean payOrder(String orderId, String userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));

        if (!order.getUserId().equals(userId)) {
            return false;
        }

        if (!order.getPaymentStatus().equals("UNPAID")) {
            return false;
        }

        order.setPaymentStatus("PAID");
        orderRepository.save(order);

        // Cập nhật lịch sử đơn hàng
        orderHistoryRepository.save(new OrderHistory(null, orderId, "PAID", "Đơn hàng đã được thanh toán.", LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))));

        return true;
    }

    public OrderResponse payOrderOnline(String orderId, String userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));

        // Kiểm tra quyền
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền thanh toán đơn hàng này");
        }

        // Kiểm tra paymentMethod
        if (!"ONLINE".equalsIgnoreCase(order.getPaymentMethod())) {
            throw new RuntimeException("Đơn hàng không hỗ trợ thanh toán online");
        }

        // Kiểm tra paymentStatus
        if ("PAID".equalsIgnoreCase(order.getPaymentStatus())) {
            throw new RuntimeException("Đơn hàng đã thanh toán trước đó");
        }

        // Cập nhật trạng thái thanh toán
        order.setPaymentStatus("PAID");
        orderRepository.save(order);

        // Ghi lịch sử trạng thái PAYMENT_SUCCESS
        orderHistoryRepository.save(new OrderHistory(null, orderId, "PAID", "Thanh toán online thành công.", LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))));

        return OrderMapper.toResponse(order);
    }

    public List<OrderResponse> getOrdersByUsername(String username) {
        return orderRepository.findByUserId(username)
                .stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersByRestaurantId(String restaurantId) {
        return orderRepository.findByRestaurantId(restaurantId)
                .stream()
                .map(OrderMapper::toResponse)
                .collect(Collectors.toList());
    }

    private double fetchMenuItemPrice(String restaurantId, String menuItemId) {
        return restaurantClient.getMenuItemById(restaurantId, menuItemId).getPrice();
    }

    private double applyPromotion(String restaurantId, String menuItemId, double basePrice) {
        try {
            PromotionDTO promotion = restaurantClient.getPromotion(restaurantId, menuItemId);

            if (promotion != null) {
                LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
                if ((today.isEqual(promotion.getStartDate()) || today.isAfter(promotion.getStartDate())) &&
                        (today.isEqual(promotion.getEndDate()) || today.isBefore(promotion.getEndDate()))) {
                    return basePrice * (1 - promotion.getDiscountPercent() / 100.0);
                }
            }
        } catch (Exception e) {
            System.out.println("⚠ Không tìm thấy khuyến mãi: " + e.getMessage());
        }
        return basePrice;
    }



    public OrderResponse createOrderFromCart(String userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserId(userId);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Giỏ hàng trống");
        }

        // ✅ Kiểm tra tất cả món cùng nhà hàng
        String restaurantId = cartItems.get(0).getRestaurantId();
        boolean sameRestaurant = cartItems.stream()
                .allMatch(item -> item.getRestaurantId().equals(restaurantId));
        if (!sameRestaurant) {
            throw new RuntimeException("Tất cả món ăn trong đơn hàng phải thuộc cùng một nhà hàng");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        double total = 0.0;

        for (CartItem cartItem : cartItems) {
            double price = fetchMenuItemPrice(restaurantId, cartItem.getMenuItemId());
            double discounted = applyPromotion(restaurantId, cartItem.getMenuItemId(), price);

            OrderItem orderItem = new OrderItem();
            orderItem.setMenuItemId(cartItem.getMenuItemId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtOrderTime(discounted);
            total += discounted * cartItem.getQuantity();

            orderItems.add(orderItem);
        }

        // ✅ Tạo đơn hàng
        Order order = new Order();
        order.setUserId(userId);
        order.setRestaurantId(restaurantId);
        order.setItems(orderItems);
        order.setTotalPrice(total);
        order.setStatus(OrderStatus.CREATED.name());
        order.setCreatedAt(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")));
        order.setPaymentStatus("UNPAID");

        // ✅ Tính lợi nhuận
        RestaurantDTO restaurant = restaurantClient.getRestaurantById(restaurantId);
        double commissionRate = (restaurant.getCommissionRate() != null) ? restaurant.getCommissionRate() : 0.3;

        double platformProfit = total * commissionRate;
        double restaurantRevenue = total - platformProfit;

        order.setCommissionRate(commissionRate);
        order.setPlatformProfit(platformProfit);
        order.setRestaurantRevenue(restaurantRevenue);

        Order savedOrder = orderRepository.save(order);

        // ✅ Ghi lịch sử trạng thái
        orderHistoryRepository.save(new OrderHistory(null, savedOrder.getId(), OrderStatus.CREATED.name(),
                "Đơn hàng đã được tạo.", LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))));

        // ✅ Gửi Kafka
        kafkaProducerService.sendOrderCreatedEvent(
                new OrderCreatedEvent(
                        savedOrder.getId(),
                        savedOrder.getRestaurantId(),
                        savedOrder.getItems().stream()
                                .map(i -> new OrderCreatedEvent.OrderItemKafka(i.getMenuItemId(), i.getQuantity()))
                                .toList()
                )
        );

        // ✅ Xoá giỏ hàng của nhà hàng đó
        cartItemRepository.deleteAll(cartItems);

        return OrderMapper.toResponse(savedOrder);
    }

    public List<TopSoldItemDTO> findTopMenuItems(int limit) {
        return orderRepositoryCustom.findTopSellingMenuItems(limit);
    }

    public List<TopSoldItemDTO> getTopSellingMenuItems(String restaurantId, LocalDate from, LocalDate to, int limit) {
        List<TopSoldItemDTO> rawList = orderRepositoryCustom.findTopSellingMenuItems(restaurantId, from, to, limit);

        return rawList.stream().map(item -> {
            try {
                MenuItemDTO menuItem = restaurantClient.getMenuItemById(restaurantId, item.getMenuItemId());
                item.setName(menuItem.getName());
            } catch (Exception e) {
                item.setName("Unknown");
            }

            System.out.println("👉 [ORDER_SERVICE] Received request to /top-menu-items");
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("👤 Auth: " + auth.getName());
            System.out.println("🎭 Roles: " + auth.getAuthorities());

            return item;
        }).collect(Collectors.toList());
    }


    public RevenueStatsDTO getRevenueStatsForRestaurant(String restaurantId, LocalDate from, LocalDate to) {
        List<Criteria> criteria = new ArrayList<>();
        criteria.add(Criteria.where("restaurantId").is(restaurantId));
        criteria.add(Criteria.where("createdAt").gte(from.atStartOfDay()).lte(to.atTime(23, 59, 59)));

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(new Criteria().andOperator(criteria.toArray(new Criteria[0]))),
                Aggregation.group()
                        .sum("totalPrice").as("totalRevenue")
                        .sum("restaurantRevenue").as("restaurantRevenue")
                        .sum("platformProfit").as("platformProfit")
        );

        AggregationResults<RevenueStatsDTO> result = mongoTemplate.aggregate(agg, "orders", RevenueStatsDTO.class);
        return result.getUniqueMappedResult() != null ? result.getUniqueMappedResult() : new RevenueStatsDTO();
    }

    public OrderStatusStatsDTO getOrderStatusStatsForRestaurant(String restaurantId, LocalDate from, LocalDate to) {
        Criteria base = Criteria.where("restaurantId").is(restaurantId)
                .and("createdAt").gte(from.atStartOfDay()).lte(to.atTime(23, 59, 59));

        List<Order> orders = mongoTemplate.find(Query.query(base), Order.class);

        OrderStatusStatsDTO dto = new OrderStatusStatsDTO();
        dto.setTotalOrders(orders.size());
        dto.setDelivered(orders.stream().filter(o -> "DELIVERED".equals(o.getStatus())).count());
        dto.setCanceled(orders.stream().filter(o -> "CANCELED".equals(o.getStatus())).count());
        return dto;
    }

    public List<DailyRevenueDTO> getRevenueByDay(String restaurantId, LocalDate from, LocalDate to) {
        return orderRepositoryCustom.getDailyRevenue(restaurantId, from, to);
    }

    public List<TopRestaurantDTO> getTopRestaurants(LocalDate from, LocalDate to, int limit) {
        List<TopRestaurantDTO> rawList = orderRepositoryCustom.findTopRestaurantsByRevenue(from, to, limit);

        return rawList.stream().map(item -> {
            try {
                RestaurantDTO res = restaurantClient.getRestaurantById(item.getRestaurantId());
                item.setRestaurantName(res.getName());
            } catch (Exception e) {
                item.setRestaurantName("Unknown");
            }
            return item;
        }).collect(Collectors.toList());
    }

    public List<DailyRevenueDTO> getSystemRevenueByDay(LocalDate from, LocalDate to) {
        return orderRepositoryCustom.getSystemRevenueByDay(from, to);
    }

    public List<PeriodRevenueDTO> getRevenueByMonth(String from, String to) {
        return orderRepositoryCustom.getRevenueByMonth(from, to);
    }

    public List<PeriodRevenueDTO> getRevenueByYear(String from, String to) {
        return orderRepositoryCustom.getRevenueByYear(from, to);
    }

}