package iuh.fit.order_service.impl;

import com.mongodb.client.AggregateIterable;
import iuh.fit.order_service.dto.DailyRevenueDTO;
import iuh.fit.order_service.dto.PeriodRevenueDTO;
import iuh.fit.order_service.dto.TopRestaurantDTO;
import iuh.fit.order_service.dto.TopSoldItemDTO;
import iuh.fit.order_service.repository.OrderRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepositoryImpl implements OrderRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<TopSoldItemDTO> findTopSellingMenuItems(int limit) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.unwind("items"),
                Aggregation.group("items.menuItemId")
                        .sum("items.quantity").as("totalSold"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "totalSold")),
                Aggregation.limit(limit)
        );

        AggregationResults<TopSoldItemDTO> results = mongoTemplate.aggregate(
                agg,
                "orders",                // ✅ Tên collection
                TopSoldItemDTO.class    // ✅ Kết quả map vào DTO
        );

        return results.getMappedResults();
    }

    @Override
    public List<TopSoldItemDTO> findTopSellingMenuItems(String restaurantId, LocalDate from, LocalDate to, int limit) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("restaurantId").is(restaurantId)
                        .and("createdAt").gte(from.atStartOfDay()).lte(to.atTime(23, 59, 59))),
                Aggregation.unwind("items"),
                Aggregation.group("items.menuItemId")
                        .sum("items.quantity").as("totalSold"),
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "totalSold")),
                Aggregation.limit(limit),
                Aggregation.project("totalSold").and("_id").as("menuItemId")
        );

        AggregationResults<TopSoldItemDTO> results = mongoTemplate.aggregate(
                agg,
                "orders",
                TopSoldItemDTO.class
        );

        return results.getMappedResults();
    }

    @Override
    public List<DailyRevenueDTO> getDailyRevenue(String restaurantId, LocalDate from, LocalDate to) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("restaurantId").is(restaurantId)
                        .and("createdAt").gte(from.atStartOfDay()).lte(to.atTime(23, 59, 59))),
                Aggregation.project("totalPrice")
                        .andExpression("dateToString('%Y-%m-%d', createdAt)").as("date"),
                Aggregation.group("date").sum("totalPrice").as("totalRevenue"),
                Aggregation.sort(Sort.by(Sort.Direction.ASC, "_id")),
                Aggregation.project("totalRevenue").and("_id").as("date") // ✅ fix chỗ này
        );


        AggregationResults<DailyRevenueDTO> results = mongoTemplate.aggregate(
                agg, "orders", DailyRevenueDTO.class
        );

        return results.getMappedResults();
    }

    @Override
    public List<TopRestaurantDTO> findTopRestaurantsByRevenue(LocalDate from, LocalDate to, int limit) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("createdAt")
                        .gte(from.atStartOfDay())
                        .lte(to.atTime(23, 59, 59))),
                Aggregation.group("restaurantId")
                        .sum("totalPrice").as("totalRevenue"), // ✅ sửa chỗ này
                Aggregation.sort(Sort.by(Sort.Direction.DESC, "totalRevenue")),
                Aggregation.limit(limit),
                Aggregation.project("totalRevenue").and("_id").as("restaurantId")
        );

        return mongoTemplate.aggregate(agg, "orders", TopRestaurantDTO.class).getMappedResults();
    }

    @Override
    public List<DailyRevenueDTO> getSystemRevenueByDay(LocalDate from, LocalDate to) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("createdAt").gte(from.atStartOfDay()).lte(to.atTime(23, 59, 59))),
                Aggregation.project("totalPrice")
                        .andExpression("dateToString('%Y-%m-%d', createdAt)").as("date"),
                Aggregation.group("date")
                        .sum("totalPrice").as("totalRevenue"),
                Aggregation.sort(Sort.by(Sort.Direction.ASC, "_id")),
                Aggregation.project("totalRevenue").and("_id").as("date")
        );

        return mongoTemplate.aggregate(agg, "orders", DailyRevenueDTO.class).getMappedResults();
    }

    @Override
    public List<PeriodRevenueDTO> getRevenueByMonth(String from, String to) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("createdAt")
                        .gte(LocalDate.parse(from + "-01").atStartOfDay())
                        .lte(LocalDate.parse(to + "-01").withDayOfMonth(28).plusDays(4).withDayOfMonth(1).atTime(23,59,59))),
                Aggregation.project("totalPrice")
                        .andExpression("dateToString('%Y-%m', createdAt)").as("period"),
                Aggregation.group("period").sum("totalPrice").as("totalRevenue"),
                Aggregation.sort(Sort.by(Sort.Direction.ASC, "_id")),
                Aggregation.project("totalRevenue").and("_id").as("period")
        );

        return mongoTemplate.aggregate(agg, "orders", PeriodRevenueDTO.class).getMappedResults();
    }

    @Override
    public List<PeriodRevenueDTO> getRevenueByYear(String from, String to) {
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("createdAt")
                        .gte(LocalDate.parse(from + "-01-01").atStartOfDay())
                        .lte(LocalDate.parse(to + "-12-31").atTime(23, 59, 59))),
                Aggregation.project("totalPrice")
                        .andExpression("dateToString('%Y', createdAt)").as("period"),
                Aggregation.group("period").sum("totalPrice").as("totalRevenue"),
                Aggregation.sort(Sort.by(Sort.Direction.ASC, "_id")),
                Aggregation.project("totalRevenue").and("_id").as("period")
        );

        return mongoTemplate.aggregate(agg, "orders", PeriodRevenueDTO.class).getMappedResults();
    }


}

