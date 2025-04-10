package iuh.fit.order_service.security;

import iuh.fit.order_service.kafka.OrderCreatedEvent;
import iuh.fit.order_service.kafka.OrderStatusUpdateEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;


    @Bean
    public ProducerFactory<String, OrderCreatedEvent> orderCreatedProducerFactory() {
        Map<String, Object> props = baseProps();
        // TẮT ghi kèm class type header
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(props, new StringSerializer(), new JsonSerializer<>());
    }

    @Bean
    public KafkaTemplate<String, OrderCreatedEvent> orderCreatedKafkaTemplate() {
        return new KafkaTemplate<>(orderCreatedProducerFactory());
    }

    @Bean
    public ProducerFactory<String, OrderStatusUpdateEvent> orderStatusProducerFactory() {
        Map<String, Object> props = baseProps();
        // TẮT ghi kèm class type header
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(props, new StringSerializer(), new JsonSerializer<>());
    }

    @Bean
    public KafkaTemplate<String, OrderStatusUpdateEvent> orderStatusKafkaTemplate() {
        return new KafkaTemplate<>(orderStatusProducerFactory());
    }

    private Map<String, Object> baseProps() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return configProps;
    }
}