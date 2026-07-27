package com.proj.webprojrct.loyalty.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * DailyCheckinReward Entity - Cấu hình phần thưởng cho mỗi ngày
 */
@Entity
@Table(name = "daily_checkin_rewards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyCheckinReward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "day_number", nullable = false, unique = true)
    private Integer dayNumber; // 1-7

    @Column(name = "reward_amount", nullable = false)
    private Integer rewardAmount; // Số coin

    @Column(name = "is_bonus")
    private Boolean isBonus; // Ngày đặc biệt

    @Column(length = 255)
    private String description;
}
