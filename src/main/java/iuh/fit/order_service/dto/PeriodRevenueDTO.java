package iuh.fit.order_service.dto;

import lombok.Data;

@Data
public class PeriodRevenueDTO {
    private String period; // yyyy-MM (tháng) hoặc yyyy (năm)
    private double totalRevenue;
}
