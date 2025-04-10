package iuh.fit.order_service.controller;

import iuh.fit.order_service.dto.DailyRevenueDTO;
import iuh.fit.order_service.dto.PeriodRevenueDTO;
import iuh.fit.order_service.dto.TopRestaurantDTO;
import iuh.fit.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class OrderAnalyticsAdminController {

    private final OrderService orderService;

    @GetMapping("/top-restaurants")
    public ResponseEntity<List<TopRestaurantDTO>> getTopRestaurants(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "5") int limit
    ) {
        List<TopRestaurantDTO> topRestaurants = orderService.getTopRestaurants(from, to, limit);
        return ResponseEntity.ok(topRestaurants);
    }

    @GetMapping("/revenue-by-day")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<DailyRevenueDTO>> getSystemRevenueByDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(orderService.getSystemRevenueByDay(from, to));
    }

    @GetMapping("/revenue-by-month")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<PeriodRevenueDTO>> getRevenueByMonth(
            @RequestParam String from, @RequestParam String to
    ) {
        return ResponseEntity.ok(orderService.getRevenueByMonth(from, to));
    }

    @GetMapping("/revenue-by-year")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<PeriodRevenueDTO>> getRevenueByYear(
            @RequestParam String from, @RequestParam String to
    ) {
        return ResponseEntity.ok(orderService.getRevenueByYear(from, to));
    }

}
