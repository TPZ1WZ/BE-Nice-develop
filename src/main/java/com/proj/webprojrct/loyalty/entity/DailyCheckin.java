package com.proj.webprojrct.loyalty.entity;

import com.proj.webprojrct.common.entity.BaseEntity;
import com.proj.webprojrct.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * DailyCheckin Entity - Lưu lịch sử checkin hàng ngày
 */
@Entity
@Table(name = "daily_checkins", 
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "checkin_date"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyCheckin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "checkin_date", nullable = false)
    private LocalDate checkinDate;

    @Column(name = "reward_amount", nullable = false)
    private Integer rewardAmount; // Số coin nhận được

    @Column(name = "current_streak", nullable = false)
    private Integer currentStreak; // Chuỗi ngày liên tiếp hiện tại

    @PrePersist
    protected void onCreate() {
        if (checkinDate == null) {
            checkinDate = LocalDate.now();
        }
    }
}
