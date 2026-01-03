package com.proj.webprojrct.luckywheel.repository;

import com.proj.webprojrct.luckywheel.entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
    
    /**
     * Lấy tất cả voucher trong ví của user (chưa dùng và còn hạn)
     */
    @Query("SELECT uc FROM UserCoupon uc WHERE uc.user.id = :userId " +
           "AND uc.isUsed = false AND uc.expiresAt > :now " +
           "ORDER BY uc.expiresAt ASC")
    List<UserCoupon> findValidCouponsByUserId(@Param("userId") Long userId, 
                                               @Param("now") LocalDateTime now);
    
    /**
     * Lấy tất cả voucher của user (cả đã dùng và hết hạn)
     */
    List<UserCoupon> findByUserIdOrderBySavedAtDesc(Long userId);
    
    /**
     * Tìm voucher theo mã và user
     */
    Optional<UserCoupon> findByCouponCodeAndUserId(String couponCode, Long userId);
    
    /**
     * Kiểm tra user đã lưu voucher từ spin history chưa
     */
    boolean existsBySpinHistoryIdAndUserId(Long spinHistoryId, Long userId);
    
    /**
     * Lấy voucher đã dùng của user
     */
    List<UserCoupon> findByUserIdAndIsUsedTrueOrderByUsedAtDesc(Long userId);
    
    /**
     * Lấy voucher hết hạn của user
     */
    @Query("SELECT uc FROM UserCoupon uc WHERE uc.user.id = :userId " +
           "AND uc.isUsed = false AND uc.expiresAt <= :now " +
           "ORDER BY uc.expiresAt DESC")
    List<UserCoupon> findExpiredCouponsByUserId(@Param("userId") Long userId, 
                                                 @Param("now") LocalDateTime now);
}
