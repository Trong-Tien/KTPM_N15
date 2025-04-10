package iuh.fit.order_service.controller;


import iuh.fit.order_service.dto.DailyRevenueDTO;
import iuh.fit.order_service.dto.TopSoldItemDTO;
import iuh.fit.order_service.service.OrderService;
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
public class OrderAnalyticsManagerController {

    private final OrderService orderService;

    // Đã có API /revenue rồi

    @GetMapping("/stats")
    public ResponseEntity<OrderStatusStatsDTO> getOrderStatusStats(
            @RequestParam String restaurantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        OrderStatusStatsDTO stats = orderService.getOrderStatusStatsForRestaurant(restaurantId, from, to);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/top-menu-items")
    public ResponseEntity<List<TopSoldItemDTO>> getTopSellingMenuItems(
            @RequestParam String restaurantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return ResponseEntity.ok(orderService.getTopSellingMenuItems(restaurantId, from, to, limit));
    }

    @GetMapping("/revenue-by-day")
    public ResponseEntity<List<DailyRevenueDTO>> getRevenueByDay(
            @RequestParam String restaurantId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(orderService.getRevenueByDay(restaurantId, from, to));
    }

}
