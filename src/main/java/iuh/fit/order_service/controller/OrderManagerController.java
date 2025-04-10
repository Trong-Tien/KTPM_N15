package iuh.fit.order_service.controller;

import iuh.fit.order_service.client.RestaurantClient;
import iuh.fit.order_service.dto.OrderResponse;
import iuh.fit.order_service.dto.RestaurantDTO;
import iuh.fit.order_service.dto.RevenueStatsDTO;
import iuh.fit.order_service.dto.UpdateOrderStatusDTO;
import iuh.fit.order_service.model.Order;
import iuh.fit.order_service.repository.OrderRepository;
import iuh.fit.order_service.security.JwtTokenUtil;
import iuh.fit.order_service.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/manager/order")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_MANAGER')")
public class OrderManagerController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final RestaurantClient restaurantClient;
    private final JwtTokenUtil jwtTokenUtil;

    // ✅ Lấy danh sách đơn hàng theo nhà hàng (và xác thực quyền)
    @GetMapping("/restaurant/{restaurantId}")
    public ResponseEntity<?> getOrdersByRestaurant(@PathVariable String restaurantId, HttpServletRequest request) {
        String managerId = extractUserIdFromToken(request);
        RestaurantDTO restaurant = restaurantClient.getRestaurantById(restaurantId);

        if (!restaurant.getManagerId().equals(managerId)) {
            return ResponseEntity.status(403).body("Bạn không có quyền truy cập nhà hàng này.");
        }

        List<OrderResponse> orders = orderService.getOrdersByRestaurantId(restaurantId);
        return ResponseEntity.ok(orders);
    }

    // ✅ Cập nhật trạng thái đơn hàng (chỉ khi manager sở hữu nhà hàng)
    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable String orderId,
            @Valid @RequestBody UpdateOrderStatusDTO request,
            HttpServletRequest httpRequest
    ) {
        String managerId = extractUserIdFromToken(httpRequest);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        // Gọi restaurant để kiểm tra chủ sở hữu
        RestaurantDTO restaurant = restaurantClient.getRestaurantById(order.getRestaurantId());
        if (!restaurant.getManagerId().equals(managerId)) {
            return ResponseEntity.status(403).body("Bạn không có quyền cập nhật đơn hàng này.");
        }

        OrderResponse updated = orderService.updateOrderStatus(orderId, request.getStatus());
        return ResponseEntity.ok(updated);
    }

    // ✅ Hàm lấy userId từ JWT token
    private String extractUserIdFromToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            return jwtTokenUtil.getUsername(token);
        }
        return null;
    }

    @GetMapping("/revenue")
    public ResponseEntity<RevenueStatsDTO> getRevenue(
            @RequestParam String restaurantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        RevenueStatsDTO stats = orderService.getRevenueStatsForRestaurant(restaurantId, from, to);
        return ResponseEntity.ok(stats);
    }
}
