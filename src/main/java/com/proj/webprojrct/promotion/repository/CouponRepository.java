package com.proj.webprojrct.promotion.repository;

import com.proj.webprojrct.promotion.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {
    @Query("""
        SELECT c FROM Coupon c
        WHERE c.isActive = true
          AND (c.startDate IS NULL OR c.startDate <= CURRENT_TIMESTAMP)
          AND (c.endDate IS NULL OR c.endDate >= CURRENT_TIMESTAMP)
          AND (c.usageLimit > c.usedCount)
          AND (:orderAmount IS NULL OR c.minOrderAmount <= :orderAmount)
        """)
    List<Coupon> findValidCouponsV2(@Param("orderAmount") Double orderAmount);

    // Tìm coupon theo code
    Optional<Coupon> findByCode(String code);

    // Tìm coupon theo code và active
    Optional<Coupon> findByCodeAndIsActive(String code, Boolean isActive);

    // Kiểm tra code đã tồn tại
    boolean existsByCode(String code);

    // Kiểm tra code đã tồn tại (trừ ID hiện tại - dùng cho update)
    boolean existsByCodeAndIdNot(String code, Long id);

    // Tìm coupon theo active status
    List<Coupon> findByIsActive(Boolean isActive);

    // Tìm coupon còn hiệu lực
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND c.startDate <= :now AND c.endDate >= :now AND c.usedCount < c.usageLimit")
    List<Coupon> findValidCoupons(@Param("now") LocalDateTime now);

    // Tìm coupon hết hạn
    @Query("SELECT c FROM Coupon c WHERE c.endDate < :now OR c.usedCount >= c.usageLimit")
    List<Coupon> findExpiredCoupons(@Param("now") LocalDateTime now);

    // Tìm coupon theo tên (search)
    @Query("SELECT c FROM Coupon c WHERE c.name LIKE CONCAT('%', :keyword, '%') OR c.code LIKE CONCAT('%', :keyword, '%')")
    Page<Coupon> findByNameOrCodeContaining(@Param("keyword") String keyword, Pageable pageable);

    // Tìm coupon theo loại giảm giá
    Page<Coupon> findByDiscountType(Coupon.DiscountType discountType, Pageable pageable);

    // Thống kê số lượng coupon theo trạng thái
    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.isActive = :isActive")
    Long countByIsActive(@Param("isActive") Boolean isActive);

    // Thống kê coupon còn hiệu lực
    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.isActive = true AND c.startDate <= :now AND c.endDate >= :now AND c.usedCount < c.usageLimit")
    Long countValidCoupons(@Param("now") LocalDateTime now);
}