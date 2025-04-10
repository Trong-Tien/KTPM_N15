package iuh.fit.order_service.repository;

import iuh.fit.order_service.dto.DailyRevenueDTO;
import iuh.fit.order_service.dto.PeriodRevenueDTO;
import iuh.fit.order_service.dto.TopRestaurantDTO;
import iuh.fit.order_service.dto.TopSoldItemDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepositoryCustom {
    List<TopSoldItemDTO> findTopSellingMenuItems(int limit);
    List<TopSoldItemDTO> findTopSellingMenuItems(String restaurantId, LocalDate from, LocalDate to, int limit);
    List<DailyRevenueDTO> getDailyRevenue(String restaurantId, LocalDate from, LocalDate to);
    List<TopRestaurantDTO> findTopRestaurantsByRevenue(LocalDate from, LocalDate to, int limit);
    List<DailyRevenueDTO> getSystemRevenueByDay(LocalDate from, LocalDate to);
    List<PeriodRevenueDTO> getRevenueByMonth(String from, String to);
    List<PeriodRevenueDTO> getRevenueByYear(String from, String to);



}
