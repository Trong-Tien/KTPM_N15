package iuh.fit.order_service.service;

import iuh.fit.order_service.dto.CartItemRequest;
import iuh.fit.order_service.dto.CartItemResponse;
import iuh.fit.order_service.model.CartItem;
import iuh.fit.order_service.repository.CartItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;

    public List<CartItemResponse> getCartByUser(String userId) {
        return cartItemRepository.findByUserId(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CartItemResponse addToCart(String userId, CartItemRequest request) {
        // 🛑 Kiểm tra nếu giỏ đã có món từ nhà hàng khác
        List<CartItem> existingItems = cartItemRepository.findByUserId(userId);

        if (!existingItems.isEmpty()) {
            String currentRestaurantId = existingItems.get(0).getRestaurantId();
            boolean sameRestaurant = existingItems.stream()
                    .allMatch(item -> item.getRestaurantId().equals(request.getRestaurantId()));

            if (!sameRestaurant) {
                throw new RuntimeException("Bạn chỉ có thể đặt món từ một nhà hàng tại một thời điểm.");
            }
        }

        // ✅ Thêm món nếu hợp lệ
        CartItem cartItem = cartItemRepository.findByUserIdAndMenuItemId(userId, request.getMenuItemId())
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setUserId(userId);
                    newItem.setMenuItemId(request.getMenuItemId());
                    newItem.setRestaurantId(request.getRestaurantId());
                    newItem.setQuantity(0);
                    newItem.setAddedAt(LocalDateTime.now());
                    return newItem;
                });

        cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        return toResponse(cartItemRepository.save(cartItem));
    }


    public void updateQuantity(String userId, CartItemRequest request) {
        CartItem item = cartItemRepository.findByUserIdAndMenuItemId(userId, request.getMenuItemId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món trong giỏ"));

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);
    }

    public void removeItem(String userId, String menuItemId) {
        CartItem item = cartItemRepository.findByUserIdAndMenuItemId(userId, menuItemId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy món này trong giỏ của bạn"));

        cartItemRepository.delete(item);
    }


    public void clearCart(String userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    private CartItemResponse toResponse(CartItem item) {
        CartItemResponse res = new CartItemResponse();
        res.setId(item.getId());
        res.setRestaurantId(item.getRestaurantId());
        res.setMenuItemId(item.getMenuItemId());
        res.setQuantity(item.getQuantity());
        res.setAddedAt(item.getAddedAt());
        return res;
    }

    public List<CartItemResponse> getCartByUserAndRestaurant(String userId, String restaurantId) {
        return cartItemRepository.findByUserId(userId).stream()
                .filter(item -> item.getRestaurantId().equals(restaurantId))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

}
