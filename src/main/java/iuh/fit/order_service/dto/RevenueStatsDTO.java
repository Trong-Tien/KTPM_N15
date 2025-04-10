package iuh.fit.order_service.dto;

import lombok.Data;

@Data
public class RevenueStatsDTO {
    private double totalRevenue;
    private double platformProfit;
    private double restaurantRevenue;
}
