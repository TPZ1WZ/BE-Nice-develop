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
 * SpinHistory Entity - Lưu lịch sử quay của user
 */
@Entity
@Table(name = "spin_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpinHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prize_id", nullable = false)
    private Prize prize;

    @Column(nullable = false)
    private LocalDateTime spinTime; // Thời gian quay

    @Column
    private String prizeCode; // Mã voucher/freeship nếu có

    @Column(nullable = false)
    private Boolean isClaimed = false; // Đã nhận thưởng chưa

    @Column
    private LocalDateTime claimedTime; // Thời gian nhận thưởng

    @PrePersist
    protected void onCreate() {
        spinTime = LocalDateTime.now();
    }
}
