package iuh.fit.order_service.controller;

import iuh.fit.order_service.dto.OrderResponse;
import iuh.fit.order_service.dto.UpdateOrderStatusDTO;
import iuh.fit.order_service.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class OrderAdminController {

    private final OrderService orderService;

    //  Lấy tất cả đơn hàng
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    //  Admin xác nhận hoặc từ chối đơn hàng
    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable String orderId,
            @Valid @RequestBody UpdateOrderStatusDTO request) {

        OrderResponse updated = orderService.updateOrderStatus(orderId, request.getStatus());
        return ResponseEntity.ok(updated);
    }

    //  Lọc đơn hàng theo trạng thái
    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable String status) {
        return ResponseEntity.ok(orderService.getOrdersByStatus(status));
    }

    // Xem tất cả đơn hàng của 1 người dùng
    @GetMapping("/user/{username}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUsername(@PathVariable String username) {
        return ResponseEntity.ok(orderService.getOrdersByUsername(username));
    }


}
