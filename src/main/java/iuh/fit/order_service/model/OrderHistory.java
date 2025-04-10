package iuh.fit.order_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "order_history")
public class OrderHistory {
    @Id
    private String id;
    private String orderId;
    private String status;
    private String description;
    private LocalDateTime changedAt;

}
