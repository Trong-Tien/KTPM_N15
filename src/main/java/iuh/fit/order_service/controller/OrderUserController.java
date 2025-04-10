package iuh.fit.order_service.controller;

import iuh.fit.order_service.dto.CreateOrderRequest;
import iuh.fit.order_service.dto.OrderDTO;
import iuh.fit.order_service.dto.OrderResponse;
import iuh.fit.order_service.model.OrderHistory;
import iuh.fit.order_service.repository.OrderHistoryRepository;
import iuh.fit.order_service.security.JwtTokenUtil;
import iuh.fit.order_service.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
public class OrderUserController {

    private final OrderHistoryRepository orderHistoryRepository;

    private final OrderService orderService;
    private final JwtTokenUtil jwtTokenUtil;

    // Tạo đơn hàng (theo chuẩn mới)
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest request, HttpServletRequest httpRequest) {
        String userId = extractUserIdFromToken(httpRequest);
        OrderResponse response = orderService.createOrder(userId, request);
        return ResponseEntity.ok(response);
    }

    // ✅ Trả về danh sách đơn hàng dạng OrderResponse
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getUserOrders(HttpServletRequest request) {
        String userId = extractUserIdFromToken(request);
        List<OrderResponse> orders = orderService.getOrdersByUser(userId);
        return ResponseEntity.ok(orders);
    }

    // ✅ Trả về 1 đơn hàng cụ thể
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable String orderId, HttpServletRequest request) {
        String userId = extractUserIdFromToken(request);
        OrderResponse order = orderService.getOrderById(orderId);
        if (!order.getUserId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(order);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> cancelOrder(@PathVariable String orderId, HttpServletRequest request) {
        String userId = extractUserIdFromToken(request);
        boolean canceled = orderService.cancelOrder(orderId, userId);
        if (!canceled) {
            return ResponseEntity.status(403).body("Không thể huỷ đơn đã được xác nhận hoặc không thuộc quyền của bạn.");
        }
        return ResponseEntity.ok("Đơn hàng đã được huỷ thành công.");
    }

    @GetMapping("/{orderId}/history")
    public ResponseEntity<List<OrderHistory>> getOrderHistory(@PathVariable String orderId) {
        List<OrderHistory> history = orderHistoryRepository.findByOrderIdOrderByChangedAtDesc(orderId);
        return ResponseEntity.ok(history);
    }



    private String extractUserIdFromToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            return jwtTokenUtil.getUsername(token);
        }
        return null;
    }

    @GetMapping("/check-reviewed")
    public boolean hasUserOrderedAndReceived(@RequestParam String userId,
                                             @RequestParam String menuItemId) {
        return orderService.hasUserOrderedAndReceived(userId, menuItemId);
    }


    @PutMapping("/{orderId}/pay")
    public ResponseEntity<?> payOrder(@PathVariable String orderId, HttpServletRequest request) {
        String userId = extractUserIdFromToken(request);
        boolean paid = orderService.payOrder(orderId, userId);
        if (!paid) {
            return ResponseEntity.status(403).body("Không thể thanh toán đơn này.");
        }
        return ResponseEntity.ok("Thanh toán thành công!");
    }

    @PostMapping("/{orderId}/pay")
    public ResponseEntity<OrderResponse> payOrderOnline(@PathVariable String orderId, HttpServletRequest request) {
        String userId = extractUserIdFromToken(request);
        OrderResponse paidOrder = orderService.payOrderOnline(orderId, userId);
        return ResponseEntity.ok(paidOrder);
    }

    @PostMapping("/from-cart")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<OrderResponse> createOrderFromCart(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String userId = jwtTokenUtil.getUsername(token);

        OrderResponse createdOrder = orderService.createOrderFromCart(userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }









}