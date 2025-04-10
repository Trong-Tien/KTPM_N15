package iuh.fit.order_service.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Data
@Document(collection = "orders")
public class Order {
    @Id
    private String id;

    private String userId; // ID của người dùng đặt hàng
    private String restaurantId;
    private List<OrderItem> items;

    private double totalPrice;
    private String status; // PENDING, CONFIRMED, DELIVERED, CANCELED
    private LocalDateTime createdAt;

    private String paymentMethod;  // ONLINE, CASH_ON_DELIVERY
    private String paymentStatus;  // UNPAID, PAID


    private double commissionRate;
    private double platformProfit;
    private double restaurantRevenue;


}
