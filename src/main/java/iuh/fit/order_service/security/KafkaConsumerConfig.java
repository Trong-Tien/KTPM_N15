package iuh.fit.order_service.security;

import iuh.fit.order_service.kafka.OrderStatusUpdateEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, OrderStatusUpdateEvent> orderStatusConsumerFactory() {
        JsonDeserializer<OrderStatusUpdateEvent> deserializer = new JsonDeserializer<>(OrderStatusUpdateEvent.class, false);
        deserializer.addTrustedPackages("*");
        deserializer.setRemoveTypeHeaders(true); // Không dùng __TypeId__ từ producer gửi

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "order-group");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean(name = "orderStatusKafkaListenerContainerFactory") // ✅ đặt đúng tên bean
    public ConcurrentKafkaListenerContainerFactory<String, OrderStatusUpdateEvent> orderStatusKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, OrderStatusUpdateEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(orderStatusConsumerFactory());
        return factory;
    }
}