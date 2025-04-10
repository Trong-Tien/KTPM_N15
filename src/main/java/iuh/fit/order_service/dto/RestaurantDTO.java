package iuh.fit.order_service.dto;

import lombok.Data;

@Data
public class RestaurantDTO {
    private String id;
    private String name;
    private String managerId;  // Quan trọng: dùng để kiểm tra quyền của manager
    private Double commissionRate;

}
