package iuh.fit.order_service.controller;

import lombok.Data;

@Data
public class OrderStatusStatsDTO {
    private long totalOrders;
    private long delivered;
    private long canceled;
}
