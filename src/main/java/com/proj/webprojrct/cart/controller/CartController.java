package com.proj.webprojrct.cart.controller;

import com.proj.webprojrct.cart.dto.CartDTO;
import com.proj.webprojrct.cart.dto.CartResponse;
import com.proj.webprojrct.cart.service.CartService;
import com.proj.webprojrct.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/carts")
public class CartController {
    private final CartService cartService;

    @GetMapping
    public Object getCartItems(@AuthenticationPrincipal User user) {
        return user == null
                ? new CartResponse()
                : cartService.getCartByUser(user);
    }

    @GetMapping("/count")
    public Map<String, Object> getCartCount(@AuthenticationPrincipal User user) {
        try {
            var cart = cartService.getCartByUser(user);
            if (cart != null && cart.getTotalQuantity() != null) {
                return Map.of("count", cart.getTotalQuantity());
            }
            return Map.of("count", 0);
        } catch (Exception e) {
            return Map.of("count", 0);
        }
    }

    @PostMapping("/add")
    public Object addCart(@AuthenticationPrincipal User user, @RequestBody CartDTO request) {
        if (user == null) {
            return Map.of("status", false, "message", "Chưa đăng nhập.");
        }
        cartService.addToCart(user, request.getProductId(), request.getQuantity(), request.getSize());
        return Map.of("status", "✅ Thêm vào giỏ hàng thành công");
    }

    // 🔄 2️⃣ Cập nhật số lượng sản phẩm
    @PatchMapping("/update")
    public Object updateQuantity(@AuthenticationPrincipal User user, @RequestBody CartDTO request) {
        cartService.updateQuantity(user, request.getProductId(), request.getQuantity(), request.getSize());
        return Map.of("status", "✅ Cập nhật số lượng thành công");
    }

    // ❌ 3️⃣ Xóa sản phẩm khỏi giỏ
    // ❌ 3️⃣ Xóa sản phẩm khỏi giỏ
    @DeleteMapping("/remove")
    public Object removeItem(@AuthenticationPrincipal User user, @RequestParam Long productId,
            @RequestParam String size) {
        return cartService.removeItemAndGetCart(user, productId, size);
    }

    // 🔄 4️⃣ Mua lại - Thêm tất cả sản phẩm từ đơn hàng vào giỏ
    @PostMapping("/reorder/{orderId}")
    public Object reorderFromOrder(@AuthenticationPrincipal User user, @PathVariable Long orderId) {
        if (user == null) {
            return Map.of("status", false, "message", "Chưa đăng nhập.");
        }
        var result = cartService.reorderFromOrder(user, orderId);
        return result;
    }
}
