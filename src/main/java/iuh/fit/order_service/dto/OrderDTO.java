package iuh.fit.order_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class OrderDTO {

    @NotBlank(message = "người dùng không được để trống")
    private String userId;

    @NotBlank(message = "nhà hàng không được để trống")
    private String restaurantId;

    @NotEmpty(message = "Danh sách món ăn không được để trống")
    private List<OrderItemDTO> items;

    private Double totalPrice;

    private String paymentMethod;
    private String paymentStatus;

}
