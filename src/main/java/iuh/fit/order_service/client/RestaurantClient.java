package iuh.fit.order_service.client;

import iuh.fit.order_service.dto.MenuItemDTO;
import iuh.fit.order_service.dto.PromotionDTO;
import iuh.fit.order_service.dto.RestaurantDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "restaurantservice")

public interface RestaurantClient {

    // Kiểm tra nhà hàng tồn tại
    @GetMapping("/api/restaurant/{restaurantId}/exists")
    boolean checkRestaurantExists(@PathVariable String restaurantId);

    // Lấy thông tin nhà hàng
    @GetMapping("/api/restaurant/{id}")
    RestaurantDTO getRestaurantById(@PathVariable("id") String id);

    // Kiểm tra menu item tồn tại
    @GetMapping("/api/restaurant/menu/{menuItemId}/exists")
    boolean checkMenuItemExists(@PathVariable String menuItemId);

    // Lấy thông tin chi tiết menu item
    @GetMapping("/api/restaurant/{restaurantId}/menu/{menuItemId}")
    MenuItemDTO getMenuItemById(@PathVariable String restaurantId,
                                @PathVariable String menuItemId);

    // Lấy khuyến mãi món ăn
    @GetMapping("/api/restaurant/{restaurantId}/menu/{menuItemId}/promotion")
    PromotionDTO getPromotion(@PathVariable String restaurantId,
                              @PathVariable String menuItemId);
}
