package iuh.fit.order_service.repository;

import iuh.fit.order_service.model.OrderHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface OrderHistoryRepository extends MongoRepository<OrderHistory, String> {
    List<OrderHistory> findByOrderIdOrderByChangedAtDesc(String orderId);
}
