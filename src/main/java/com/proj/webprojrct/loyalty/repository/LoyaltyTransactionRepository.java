package com.proj.webprojrct.loyalty.repository;

import com.proj.webprojrct.loyalty.entity.LoyaltyTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, Long> {

    /**
     * Lấy lịch sử giao dịch của user (phân trang)
     */
    Page<LoyaltyTransaction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Lấy tất cả giao dịch của user
     */
    List<LoyaltyTransaction> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Lấy giao dịch theo loại
     */
    List<LoyaltyTransaction> findByUserIdAndTransactionTypeOrderByCreatedAtDesc(
            Long userId,
            LoyaltyTransaction.TransactionType type
    );

    /**
     * Lấy giao dịch theo source
     */
    List<LoyaltyTransaction> findByUserIdAndSourceOrderByCreatedAtDesc(
            Long userId,
            String source
    );

    /**
     * Tính tổng điểm earned
     */
    @Query("SELECT COALESCE(SUM(lt.amount), 0) FROM LoyaltyTransaction lt " +
           "WHERE lt.user.id = :userId AND lt.transactionType = 'EARN'")
    Integer getTotalEarnedByUserId(@Param("userId") Long userId);

    /**
     * Tính tổng điểm spent
     */
    @Query("SELECT COALESCE(SUM(ABS(lt.amount)), 0) FROM LoyaltyTransaction lt " +
           "WHERE lt.user.id = :userId AND lt.transactionType = 'SPEND'")
    Integer getTotalSpentByUserId(@Param("userId") Long userId);

    /**
     * Lấy giao dịch trong khoảng thời gian
     */
    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.user.id = :userId " +
           "AND lt.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY lt.createdAt DESC")
    List<LoyaltyTransaction> findByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Tìm lô coin EARN còn hiệu lực (chưa hết hạn) và sẽ hết hạn sớm nhất
     * Chỉ lấy các transaction EARN có expiry_date chưa qua và còn dư (amount > 0)
     */
    @Query("SELECT lt FROM LoyaltyTransaction lt WHERE lt.user.id = :userId " +
           "AND lt.transactionType = 'EARN' " +
           "AND lt.expiryDate IS NOT NULL " +
           "AND lt.expiryDate > CURRENT_TIMESTAMP " +
           "ORDER BY lt.expiryDate ASC")
    List<LoyaltyTransaction> findValidEarnTransactionsByUserIdOrderByExpiryAsc(@Param("userId") Long userId);

    /**
     * Tính tổng số coin sắp hết hạn sớm nhất của user
     * (Tính từ transaction EARN có expiry_date gần nhất)
     */
    @Query("SELECT COALESCE(SUM(lt.amount), 0) FROM LoyaltyTransaction lt " +
           "WHERE lt.user.id = :userId " +
           "AND lt.transactionType = 'EARN' " +
           "AND lt.expiryDate = (" +
           "    SELECT MIN(lt2.expiryDate) FROM LoyaltyTransaction lt2 " +
           "    WHERE lt2.user.id = :userId " +
           "    AND lt2.transactionType = 'EARN' " +
           "    AND lt2.expiryDate IS NOT NULL " +
           "    AND lt2.expiryDate > CURRENT_TIMESTAMP" +
           ")")
    Integer getExpiringCoinsByEarliestDate(@Param("userId") Long userId);
}
