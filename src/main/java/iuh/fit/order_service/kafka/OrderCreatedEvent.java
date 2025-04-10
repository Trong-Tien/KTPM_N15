package iuh.fit.order_service.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreatedEvent {
    private String orderId;
    private String restaurantId;
    private List<OrderItemKafka> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemKafka {
        private String menuItemId;
        private int quantity;
    }
}
