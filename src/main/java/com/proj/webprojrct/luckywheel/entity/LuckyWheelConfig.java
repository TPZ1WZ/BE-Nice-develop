package com.proj.webprojrct.luckywheel.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * LuckyWheelConfig Entity - Cấu hình vòng quay may mắn
 */
@Entity
@Table(name = "lucky_wheel_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LuckyWheelConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_key", unique = true, nullable = false)
    private String configKey;

    @Column(name = "config_value", nullable = false)
    private String configValue;

    @Column(name = "description")
    private String description;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
