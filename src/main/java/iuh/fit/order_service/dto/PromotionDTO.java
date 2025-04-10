package iuh.fit.order_service.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PromotionDTO {
    private String id;
    private double discountPercent;
    private LocalDate startDate;
    private LocalDate endDate;
}
