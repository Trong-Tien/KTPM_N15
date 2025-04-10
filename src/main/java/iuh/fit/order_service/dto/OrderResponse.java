package iuh.fit.order_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderResponse {

    private String userId;
    private String restaurantId;
    private List<OrderItemDTO> items;
    private Double totalPrice;

    private String status;
    private String statusDescription; // ✅ Thêm mô tả trạng thái đơn hàng

    private String paymentMethod;
    private String paymentStatus;

    private double commissionRate;
    private double platformProfit;
    private double restaurantRevenue;

}
