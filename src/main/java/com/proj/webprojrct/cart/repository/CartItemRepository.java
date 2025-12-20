package com.proj.webprojrct.cart.repository;

import com.proj.webprojrct.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartIdAndSizeAndProductId(Long cartId, String size, Long productId);
    
    // Tìm tất cả cart items theo cart ID
    java.util.List<CartItem> findByCartId(Long cartId);
    
    // Tìm tất cả cart items theo product ID
    java.util.List<CartItem> findByProductId(Long productId);
    
    // Xóa tất cả cart items của một sản phẩm
    void deleteByProductId(Long productId);
    
    // Đếm số cart items của sản phẩm
    long countByProductId(Long productId);
}
