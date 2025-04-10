package iuh.fit.order_service.controller;

import iuh.fit.order_service.dto.TopSoldItemDTO;
import iuh.fit.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/order")
@RequiredArgsConstructor
public class OrderPublicController {

    private final OrderService orderService;

    @GetMapping("/top-menu-items")
    public List<TopSoldItemDTO> getTopMenuItems(@RequestParam(defaultValue = "5") int limit) {
        return orderService.findTopMenuItems(limit);
    }
}
