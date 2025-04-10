package iuh.fit.order_service.dto;

import lombok.Data;

@Data
public class TopRestaurantDTO {
    private String restaurantId;
    private double totalRevenue;
    private String restaurantName;
}
