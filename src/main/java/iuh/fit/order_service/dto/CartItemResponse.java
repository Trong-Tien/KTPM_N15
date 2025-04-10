package iuh.fit.order_service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CartItemResponse {
    private String id;
    private String restaurantId;
    private String menuItemId;
    private int quantity;
    private LocalDateTime addedAt;
}
