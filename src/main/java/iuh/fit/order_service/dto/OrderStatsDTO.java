package iuh.fit.order_service.dto;

import lombok.Data;

@Data
public class OrderStatsDTO {
    private long totalOrders;
    private double totalRevenue;
    private double platformProfit;
}

