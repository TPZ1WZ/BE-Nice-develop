package com.proj.webprojrct.luckywheel.entity;

import com.proj.webprojrct.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_bonus_spins")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBonusSpins {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "bonus_spins", nullable = false)
    private Integer bonusSpins = 0;
    
    @Column(name = "granted_at")
    private LocalDateTime grantedAt;
    
    @Column(name = "granted_by")
    private String grantedBy; // Admin username
    
    @Column(name = "reason")
    private String reason;
}
