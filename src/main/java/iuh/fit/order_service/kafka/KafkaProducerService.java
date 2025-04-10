package iuh.fit.order_service.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, OrderCreatedEvent> orderCreatedKafkaTemplate;
    private final KafkaTemplate<String, OrderStatusUpdateEvent> orderStatusKafkaTemplate;

    @Value("${app.kafka.topic.orderCreated}")
    private String orderCreatedTopic;

    @Value("${app.kafka.topic.orderStatusUpdated}")
    private String orderStatusUpdatedTopic;

    public KafkaProducerService(
            KafkaTemplate<String, OrderCreatedEvent> orderCreatedKafkaTemplate,
            KafkaTemplate<String, OrderStatusUpdateEvent> orderStatusKafkaTemplate) {
        this.orderCreatedKafkaTemplate = orderCreatedKafkaTemplate;
        this.orderStatusKafkaTemplate = orderStatusKafkaTemplate;
    }

    public void sendOrderCreatedEvent(OrderCreatedEvent event) {
        orderCreatedKafkaTemplate.send(orderCreatedTopic, event);
        System.out.println("✅ Sent Kafka OrderCreatedEvent: " + event);
    }

    public void sendOrderStatus(OrderStatusUpdateEvent event) {
        orderStatusKafkaTemplate.send(orderStatusUpdatedTopic, event);
        System.out.println("📦 Sent Kafka OrderStatusUpdateEvent: " + event);
    }
}
