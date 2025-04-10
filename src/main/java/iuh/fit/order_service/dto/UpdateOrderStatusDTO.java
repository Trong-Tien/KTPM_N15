package iuh.fit.order_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateOrderStatusDTO {

    @NotBlank(message = "Trạng thái đơn hàng không được để trống")
    private String status; // CONFIRMED, FAILED, CANCELED, DELIVERED, v.v.
}