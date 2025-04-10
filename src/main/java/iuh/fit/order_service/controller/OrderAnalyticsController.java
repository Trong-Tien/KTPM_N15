package iuh.fit.order_service.controller;

import iuh.fit.order_service.dto.OrderStatsDTO;
import iuh.fit.order_service.model.Order;
import iuh.fit.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;


@RestController
@RequestMapping("/api/admin/order")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor
public class OrderAnalyticsController {

    private final OrderRepository orderRepository;

    @GetMapping("/stats")
    public ResponseEntity<OrderStatsDTO> getOrderStats() {
        List<Order> orders = orderRepository.findAll();

        long totalOrders = orders.size();
        double totalRevenue = orders.stream().mapToDouble(Order::getTotalPrice).sum();
        double platformProfit = orders.stream().mapToDouble(Order::getPlatformProfit).sum();

        OrderStatsDTO dto = new OrderStatsDTO();
        dto.setTotalOrders(totalOrders);
        dto.setTotalRevenue(totalRevenue);
        dto.setPlatformProfit(platformProfit);

        return ResponseEntity.ok(dto);
    }
}

