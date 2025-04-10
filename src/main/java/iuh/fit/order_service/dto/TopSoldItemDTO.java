package iuh.fit.order_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopSoldItemDTO {

    @Field("_id") // ✅ vì MongoDB group by _id
    private String menuItemId;
    private long totalSold;
    private String name;


}

