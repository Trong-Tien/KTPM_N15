package iuh.fit.order_service.repository;

import iuh.fit.order_service.model.CartItem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends MongoRepository<CartItem, String> {

    List<CartItem> findByUserId(String userId);

    Optional<CartItem> findByUserIdAndMenuItemId(String userId, String menuItemId);

    void deleteByUserId(String userId);
}
