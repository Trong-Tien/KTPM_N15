package iuh.fit.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyRevenueDTO {
    private String date;           // yyyy-MM-dd
    private double totalRevenue;   // tổng tiền của ngày
}
