package iuh.fit.order_service.dto;

import lombok.Data;

@Data
public class MenuItemDTO {
    private String id;
    private String name;
    private String description;
    private Double price;
    private String imageUrl;
    private String restaurantId;
    private String categoryId;
    private int stock;
}
