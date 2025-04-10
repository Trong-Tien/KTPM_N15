package iuh.fit.order_service.mapper;

import iuh.fit.order_service.dto.OrderDTO;
import iuh.fit.order_service.dto.OrderResponse;
import iuh.fit.order_service.model.Order;
import iuh.fit.order_service.model.OrderItem;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    private double commissionRate;
    private double platformProfit;
    private double restaurantRevenue;


    public static Order toEntity(OrderDTO dto) {
        Order order = new Order();
        order.setUserId(dto.getUserId());
        order.setRestaurantId(dto.getRestaurantId());

        List<OrderItem> items = dto.getItems().stream()
                .map(OrderItemMapper::toEntity)
                .collect(Collectors.toList());

        order.setItems(items);

        double total = items.stream()
                .mapToDouble(i -> i.getPriceAtOrderTime() * i.getQuantity())
                .sum();
        order.setTotalPrice(total);

        return order;
    }

    public static OrderDTO toDTO(Order entity) {
        OrderDTO dto = new OrderDTO();
        dto.setUserId(entity.getUserId());
        dto.setRestaurantId(entity.getRestaurantId());

        dto.setItems(entity.getItems().stream()
                .map(OrderItemMapper::toDTO)
                .collect(Collectors.toList()));
        dto.setTotalPrice(entity.getTotalPrice());
        dto.setPaymentMethod(entity.getPaymentMethod());
        dto.setPaymentStatus(entity.getPaymentStatus());
        System.out.println("✅ Tổng tiền được gán vào DTO: " + entity.getTotalPrice());


        return dto;
    }

    public static OrderResponse toResponse(Order entity) {
        OrderResponse res = new OrderResponse();
        res.setUserId(entity.getUserId());
        res.setRestaurantId(entity.getRestaurantId());
        res.setItems(entity.getItems().stream()
                .map(OrderItemMapper::toDTO)
                .collect(Collectors.toList()));
        res.setTotalPrice(entity.getTotalPrice());
        res.setStatus(entity.getStatus());
        res.setStatusDescription(getStatusDescription(entity.getStatus()));
        res.setPaymentMethod(entity.getPaymentMethod());
        res.setPaymentStatus(entity.getPaymentStatus());

        res.setCommissionRate(entity.getCommissionRate());
        res.setPlatformProfit(entity.getPlatformProfit());
        res.setRestaurantRevenue(entity.getRestaurantRevenue());



        return res;
    }

    public static String getStatusDescription(String status) {
        return switch (status) {
            case "CREATED" -> "Đơn hàng đã được tạo.";
            case "CONFIRMED" -> "Nhà hàng đã xác nhận đơn hàng.";
            case "PREPARING" -> "Nhà hàng đang chuẩn bị món ăn.";
            case "DELIVERING" -> "Đơn hàng đang được giao.";
            case "DELIVERED" -> "Đơn hàng đã giao thành công.";
            case "CANCELED" -> "Đơn hàng đã bị huỷ.";
            case "FAILED" -> "Đơn hàng đã bị từ chối.";
            default -> "Không rõ trạng thái.";
        };
    }



}
