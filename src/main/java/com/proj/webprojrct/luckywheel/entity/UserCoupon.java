package com.proj.webprojrct.luckywheel.entity;

import com.proj.webprojrct.common.entity.BaseEntity;
import com.proj.webprojrct.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * UserCoupon Entity - Ví voucher của user từ vòng quay
 * Lưu trữ các voucher user nhận được và muốn dùng sau
 */
@Entity
@Table(name = "user_coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCoupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spin_history_id", nullable = false)
    private SpinHistory spinHistory;

    @Column(nullable = false)
    private String couponCode; // Mã voucher (VCE9B90107)

    @Column(nullable = false)
    private String prizeName; // Tên phần thưởng (Giảm 10%)

    @Column
    private String prizeDescription; // Mô tả phần thưởng

    @Column
    private Double discountValue; // Giá trị giảm giá

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Prize.PrizeType prizeType; // VOUCHER, FREESHIP, POINTS

    @Column(nullable = false)
    private Boolean isUsed = false; // Đã sử dụng chưa

    @Column
    private LocalDateTime usedAt; // Thời gian sử dụng

    @Column
    private Long orderId; // ID đơn hàng đã sử dụng voucher này

    @Column(nullable = false)
    private LocalDateTime expiresAt; // Thời gian hết hạn (thường 30 ngày từ khi nhận)

    @Column(nullable = false)
    private LocalDateTime savedAt; // Thời gian lưu vào ví

    @PrePersist
    protected void onCreate() {
        savedAt = LocalDateTime.now();
        if (expiresAt == null) {
            // Mặc định voucher hết hạn sau 30 ngày
            expiresAt = LocalDateTime.now().plusDays(30);
        }
    }

    /**
     * Kiểm tra voucher có còn hợp lệ không
     */
    public boolean isValid() {
        return !isUsed && LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * Đánh dấu voucher đã sử dụng
     */
    public void markAsUsed(Long orderId) {
        this.isUsed = true;
        this.usedAt = LocalDateTime.now();
        this.orderId = orderId;
    }
}
