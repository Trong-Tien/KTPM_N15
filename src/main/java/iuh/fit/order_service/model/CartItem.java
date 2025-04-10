package iuh.fit.order_service.model;


import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "cart_items")
public class CartItem {

    @Id
    private String id;
    private String userId;
    private String restaurantId;
    private String menuItemId;
    private int quantity;
    private LocalDateTime addedAt;
}
