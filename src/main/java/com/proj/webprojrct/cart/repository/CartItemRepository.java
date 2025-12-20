package com.proj.webprojrct.cart.repository;

import com.proj.webprojrct.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartIdAndSizeAndProductId(Long cartId, String size, Long productId);
    
    // Tìm tất cả items trong một cart
    List<CartItem> findAllByCartId(Long cartId);
    
    // Tìm tất cả cart items theo product ID
    List<CartItem> findByProductId(Long productId);
    
    // Xóa tất cả cart items của một sản phẩm
    void deleteByProductId(Long productId);
    
    // Đếm số cart items của sản phẩm
    long countByProductId(Long productId);
    
    // Native DELETE query - force direct SQL execution
    @Modifying
    @Query(value = "DELETE FROM cart_items WHERE cart_id = :cartId AND product_id = :productId AND size = :size", nativeQuery = true)
    int deleteByCartIdAndProductIdAndSize(@Param("cartId") Long cartId, 
                                          @Param("productId") Long productId, 
                                          @Param("size") String size);
}
