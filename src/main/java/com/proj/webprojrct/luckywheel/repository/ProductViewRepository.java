package com.proj.webprojrct.luckywheel.repository;

import com.proj.webprojrct.luckywheel.entity.ProductView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface ProductViewRepository extends JpaRepository<ProductView, Long> {
    
    /**
     * Đếm số lượng sản phẩm KHÁC NHAU đã xem hôm nay
     */
    @Query("SELECT COUNT(DISTINCT pv.product.id) FROM ProductView pv " +
           "WHERE pv.user.id = :userId " +
           "AND pv.viewDate = :viewDate")
    long countDistinctProductsViewedByUserAndDate(@Param("userId") Long userId, 
                                                   @Param("viewDate") LocalDate viewDate);
    
    /**
     * Kiểm tra đã xem sản phẩm này hôm nay chưa
     */
    @Query("SELECT COUNT(pv) > 0 FROM ProductView pv " +
           "WHERE pv.user.id = :userId " +
           "AND pv.product.id = :productId " +
           "AND pv.viewDate = :viewDate")
    boolean existsByUserAndProductAndDate(@Param("userId") Long userId, 
                                         @Param("productId") Long productId,
                                         @Param("viewDate") LocalDate viewDate);
}
