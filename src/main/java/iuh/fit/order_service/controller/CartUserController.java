package iuh.fit.order_service.controller;

import iuh.fit.order_service.dto.CartItemRequest;
import iuh.fit.order_service.dto.CartItemResponse;
import iuh.fit.order_service.security.JwtTokenUtil;
import iuh.fit.order_service.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_USER')")
public class CartUserController {

    private final CartService cartService;
    private final JwtTokenUtil jwtTokenUtil;

    // ✅ Xem giỏ hàng của user
    @GetMapping
    public ResponseEntity<List<CartItemResponse>> getCart(
            @RequestParam(required = false) String restaurantId,
            HttpServletRequest request) {

        String userId = jwtTokenUtil.getUsername(request.getHeader("Authorization").substring(7));

        if (restaurantId != null) {
            return ResponseEntity.ok(cartService.getCartByUserAndRestaurant(userId, restaurantId));
        } else {
            return ResponseEntity.ok(cartService.getCartByUser(userId));
        }
    }



    // ✅ Thêm món vào giỏ
    @PostMapping
    public ResponseEntity<CartItemResponse> addToCart(@Valid @RequestBody CartItemRequest dto,
                                                      HttpServletRequest request) {
        String userId = jwtTokenUtil.getUsername(request.getHeader("Authorization").substring(7));
        return ResponseEntity.ok(cartService.addToCart(userId, dto));
    }

    // ✅ Cập nhật số lượng món trong giỏ
    @PutMapping
    public ResponseEntity<Void> updateQuantity(@Valid @RequestBody CartItemRequest dto,
                                               HttpServletRequest request) {
        String userId = jwtTokenUtil.getUsername(request.getHeader("Authorization").substring(7));
        cartService.updateQuantity(userId, dto);
        return ResponseEntity.ok().build();
    }

    // ✅ Xoá một món ra khỏi giỏ
    @DeleteMapping("/menuItemId/{menuItemId}")
    public ResponseEntity<Void> removeItem(@PathVariable String menuItemId,
                                           HttpServletRequest request) {
        String userId = jwtTokenUtil.getUsername(request.getHeader("Authorization").substring(7));
        cartService.removeItem(userId, menuItemId);
        return ResponseEntity.ok().build();
    }

    // ✅ Xoá toàn bộ giỏ hàng
    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(HttpServletRequest request) {
        String userId = jwtTokenUtil.getUsername(request.getHeader("Authorization").substring(7));
        cartService.clearCart(userId);
        return ResponseEntity.ok().build();
    }

}
