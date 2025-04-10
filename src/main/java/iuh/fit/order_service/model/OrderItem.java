package iuh.fit.order_service.model;

import lombok.Data;

@Data
public class OrderItem {
   private String menuItemId;
   private int quantity;
   private Double priceAtOrderTime;

}
