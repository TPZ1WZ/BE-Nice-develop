package com.proj.webprojrct.loyalty.entity;

import com.proj.webprojrct.common.entity.BaseEntity;
import com.proj.webprojrct.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * LoyaltyTransaction Entity - Lịch sử giao dịch coin
 */
@Entity
@Table(name = "loyalty_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 50)
    private TransactionType transactionType;

    @Column(nullable = false)
    private Integer amount; // Dương = earn, âm = spend

    @Column(nullable = false, length = 50)
    private String source; // DAILY_CHECKIN, ORDER, REVIEW, ADMIN

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "reference_id")
    private Long referenceId; // ID của order/review/etc.

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter; // Số dư sau giao dịch

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate; // Ngày hết hạn của coin (chỉ áp dụng cho EARN)

    public enum TransactionType {
        EARN,   // Tích điểm
        SPEND,  // Tiêu điểm
        REFUND  // Hoàn điểm
    }
}
