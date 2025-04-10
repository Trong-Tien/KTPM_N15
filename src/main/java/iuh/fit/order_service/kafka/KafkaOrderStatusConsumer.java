package iuh.fit.order_service.kafka;

import iuh.fit.order_service.model.Order;
import iuh.fit.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaOrderStatusConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(
            topics = "order-status-updated",
            groupId = "order-group",
            containerFactory = "orderStatusKafkaListenerContainerFactory"
    )
    public void handleStatusUpdate(OrderStatusUpdateEvent event) {
        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            order.setStatus(event.getStatus());
            orderRepository.save(order);
            System.out.println("✅ Cập nhật trạng thái đơn hàng: " + order.getId() + " -> " + event.getStatus());
        });
    }
}