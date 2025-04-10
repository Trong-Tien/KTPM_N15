package iuh.fit.order_service.mapper;

import iuh.fit.order_service.dto.OrderItemDTO;
import iuh.fit.order_service.model.OrderItem;

public class OrderItemMapper {

    public static OrderItem toEntity(OrderItemDTO dto) {
        OrderItem item = new OrderItem();
        item.setMenuItemId(dto.getMenuItemId());
        item.setQuantity(dto.getQuantity());
        item.setPriceAtOrderTime(dto.getPriceAtOrderTime());
        return item;
    }

    public static OrderItemDTO toDTO(OrderItem entity) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setMenuItemId(entity.getMenuItemId());
        dto.setQuantity(entity.getQuantity());
        dto.setPriceAtOrderTime(entity.getPriceAtOrderTime());
        return dto;
    }


}
