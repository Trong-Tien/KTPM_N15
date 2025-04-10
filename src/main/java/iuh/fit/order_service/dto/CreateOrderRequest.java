package iuh.fit.order_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    @NotBlank(message = "Nhà hàng không được để trống")
    private String restaurantId;

    @NotEmpty(message = "Danh sách món ăn không được để trống")
    private List<OrderItemDTO> items;

    @NotBlank
    private String paymentMethod; //(ONLINE hoặc CASH_ON_DELIVERY)
}
