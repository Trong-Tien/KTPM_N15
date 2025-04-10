package iuh.fit.order_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderItemDTO {

    @NotBlank(message = "ID món ăn không được để trống")
    private String menuItemId;

    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private int quantity;

    private Double priceAtOrderTime;

}
