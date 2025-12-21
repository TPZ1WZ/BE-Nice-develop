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

    // Alias method cho compatibility với code cũ
    default List<CartItem> findByCartId(Long cartId) {
        return findAllByCartId(cartId);
    }

    // Tìm tất cả cart items theo product ID
    List<CartItem> findByProductId(Long productId);

    // Xóa tất cả cart items của một sản phẩm
    void deleteByProductId(Long productId);

    // Đếm số cart items của sản phẩm
    long countByProductId(Long productId);

    // JPQL DELETE query - safer and database agnostic
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.cart.id = :cartId AND c.product.id = :productId AND c.size = :size")
    void deleteByCartIdAndProductIdAndSize(@Param("cartId") Long cartId,
            @Param("productId") Long productId,
            @Param("size") String size);
}
