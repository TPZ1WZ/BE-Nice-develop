package com.proj.webprojrct.luckywheel.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * LuckyWheelReward Entity - Cấu hình phần thưởng vòng quay
 */
@Entity
@Table(name = "lucky_wheel_rewards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LuckyWheelReward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "position", nullable = false, unique = true)
    private Integer position; // 0-7

    @Column(name = "reward_type", nullable = false, length = 50)
    private String rewardType; // COIN, COUPON, NOTHING

    @Column(name = "coin_amount")
    private Integer coinAmount;

    @Column(name = "coupon_id")
    private Long couponId;

    @Column(name = "probability", nullable = false, precision = 5, scale = 2)
    private BigDecimal probability; // Xác suất (%) - tự động tính từ weight

    @Column(name = "weight", nullable = false)
    @Builder.Default
    private Integer weight = 10; // Trọng số để tính xác suất

    @Column(name = "icon_name", length = 100)
    private String iconName;

    @Column(name = "label")
    private String label;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
