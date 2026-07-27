package com.proj.webprojrct.luckywheel.entity;

import com.proj.webprojrct.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * SpinHistory Entity - Lịch sử quay thưởng
 */
@Entity
@Table(name = "spin_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpinHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id")
    private LuckyWheelReward reward;

    @Column(name = "reward_type", length = 50)
    private String rewardType; // COIN, COUPON, NOTHING

    @Column(name = "coin_amount")
    private Integer coinAmount;

    @Column(name = "coupon_id")
    private Long couponId;

    @Column(name = "spin_date")
    private LocalDateTime spinDate;

    @Column(name = "spin_time", nullable = false)
    private LocalDateTime spinTime;

    @Column(name = "cost")
    @Builder.Default
    private Integer cost = 0; // 0 = free, 500 = paid

    @Column(name = "is_claimed", nullable = false)
    @Builder.Default
    private Boolean isClaimed = false; // Đã nhận thưởng chưa

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        spinDate = now;
        spinTime = now;
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
