package com.proj.webprojrct.favorite.repository;

import com.proj.webprojrct.favorite.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    
    // Kiểm tra sản phẩm đã được yêu thích chưa
    boolean existsByUserIdAndProductId(Long userId, Long productId);
    
    // Tìm favorite theo user và product
    Optional<Favorite> findByUserIdAndProductId(Long userId, Long productId);
    
    // Lấy danh sách sản phẩm yêu thích của user (có phân trang)
    Page<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    // Đếm số lượng yêu thích của user
    long countByUserId(Long userId);
    
    // Lấy danh sách product IDs mà user đã yêu thích
    @Query("SELECT f.product.id FROM Favorite f WHERE f.user.id = :userId")
    List<Long> findProductIdsByUserId(@Param("userId") Long userId);
    
    // Tìm tất cả favorites theo product ID
    List<Favorite> findByProductId(Long productId);
    
    // Xóa tất cả favorites của một sản phẩm
    void deleteByProductId(Long productId);
    
    // Đếm số favorites của sản phẩm
    long countByProductId(Long productId);
}
