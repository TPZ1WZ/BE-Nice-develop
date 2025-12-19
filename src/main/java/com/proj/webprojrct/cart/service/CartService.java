package com.proj.webprojrct.cart.service;

import com.proj.webprojrct.cart.dto.CartResponse;
import com.proj.webprojrct.cart.entity.Cart;
import com.proj.webprojrct.cart.entity.CartItem;
import com.proj.webprojrct.cart.repository.CartItemRepository;
import com.proj.webprojrct.cart.repository.CartRepository;
import com.proj.webprojrct.product.repository.ProductRepository;
import com.proj.webprojrct.user.entity.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    @Transactional
    public CartResponse getCartByUser(User user) {
        System.out.println("🔍 Getting cart for user ID: " + user.getId());
        
        // Sử dụng getOrCreateCart để tránh duplicate
        Cart cart = getOrCreateCart(user);
        
        System.out.println("📦 Cart ID: " + cart.getId() + ", Items count: " + 
            (cart.getItems() != null ? cart.getItems().size() : "null"));
        
        if (cart.getItems() != null && !cart.getItems().isEmpty()) {
            System.out.println("📋 Cart items details:");
            cart.getItems().forEach(item -> {
                System.out.println("  - Product: " + item.getProduct().getName() + 
                    ", Size: " + item.getSize() + 
                    ", Quantity: " + item.getQuantity());
            });
        }
        
        return new CartResponse(cart);
    }

    @Transactional
    public void addToCart(User user, Long productId, Integer quantity, String size) {
        System.out.println("🛒 addToCart called - userId: " + user.getId() + ", productId: " + productId + ", quantity: " + quantity + ", size: " + size);
        
        var product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
        if (product.getStock() < quantity) {
            throw new RuntimeException("Quantity product not enough");
        }
        
        // Tìm hoặc tạo cart với synchronized để tránh duplicate
        Cart cart = getOrCreateCart(user);
        System.out.println("✅ Using cart with ID: " + cart.getId());
        
        // Tìm hoặc tạo cart item
        cartItemRepository.findByCartIdAndSizeAndProductId(cart.getId(), size, productId)
                .ifPresentOrElse(cartItem -> {
                    System.out.println("📦 Updating existing cart item - Old quantity: " + cartItem.getQuantity());
                    cartItem.setQuantity(cartItem.getQuantity() + quantity);
                    cartItemRepository.save(cartItem);
                    System.out.println("📦 New quantity: " + cartItem.getQuantity());
                }, () -> {
                    System.out.println("➕ Creating new cart item");
                    var newCartItem = new CartItem();
                    newCartItem.setCart(cart);
                    newCartItem.setProduct(product);
                    newCartItem.setQuantity(quantity);
                    newCartItem.setProductPrice(product.getPrice());
                    newCartItem.setTotalPrice(product.getPrice() * quantity);
                    newCartItem.setSize(size);
                    cartItemRepository.save(newCartItem);
                    System.out.println("✅ Cart item saved successfully");
                });
    }
    
    private synchronized Cart getOrCreateCart(User user) {
        // Tìm cart hiện có
        var existingCart = cartRepository.findByUserId(user.getId());
        if (existingCart.isPresent()) {
            System.out.println("✅ Found existing cart with ID: " + existingCart.get().getId());
            return existingCart.get();
        }
        
        // Nếu chưa có, thử tạo mới với error handling
        try {
            System.out.println("🆕 Creating new cart for user " + user.getId());
            var newCart = new Cart();
            newCart.setUser(user);
            var savedCart = cartRepository.save(newCart);
            System.out.println("✅ New cart created with ID: " + savedCart.getId());
            return savedCart;
        } catch (Exception e) {
            // Nếu bị duplicate key (concurrent creation), tìm lại cart
            System.out.println("⚠️ Exception creating cart: " + e.getMessage());
            System.out.println("🔄 Retrying to find existing cart...");
            
            var retryCart = cartRepository.findByUserId(user.getId());
            if (retryCart.isPresent()) {
                System.out.println("✅ Found cart on retry with ID: " + retryCart.get().getId());
                return retryCart.get();
            }
            
            // Nếu vẫn không tìm thấy, throw exception
            System.err.println("❌ Failed to get or create cart for user " + user.getId());
            throw new RuntimeException("Unable to get or create cart", e);
        }
    }

    // Cập nhật số lượng sản phẩm trong giỏ hàng
    public void updateQuantity(User user, Long productId, Integer newQuantity, String size) {
        if (newQuantity == null || newQuantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
        var product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
        if (product.getStock() < newQuantity) {
            throw new RuntimeException("Quantity product not enough");
        }
        var cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng của người dùng"));
        var cartItem = cartItemRepository.findByCartIdAndSizeAndProductId(cart.getId(), size, productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong giỏ hàng"));
        cartItem.setQuantity(newQuantity);
        cartItem.setTotalPrice(cartItem.getProductPrice() * newQuantity);
        cartItemRepository.save(cartItem);
    }

    // Xóa sản phẩm khỏi giỏ hàng
    public void removeItem(User user, Long productId, String size) {
        var cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng của người dùng"));

        var cartItem = cartItemRepository.findByCartIdAndSizeAndProductId(cart.getId(), size, productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không có trong giỏ hàng"));

        cartItemRepository.delete(cartItem);

        if (cart.getItems().isEmpty()) {
            cart.setTotalPrice(0.0);
            cart.setTotalQuantity(0);
            cartRepository.save(cart);
        }
    }


}
