package com.proj.webprojrct.notification.dto;

import com.proj.webprojrct.notification.entity.NotificationType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * NotificationResponse - DTO trả về thông tin thông báo
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {
    private Long id;
    private Long userId;
    private NotificationType type;
    private String title;
    private String message;
    private Map<String, Object> data;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Icon cho mỗi loại thông báo (frontend sẽ sử dụng)
     */
    public String getIconName() {
        return switch (type) {
            case ORDER -> "shopping_bag";
            case FAVORITE -> "favorite";
            case COUPON -> "local_offer";
            case PRODUCT -> "inventory_2";
            case SYSTEM -> "notifications";
        };
    }

    /**
     * Màu sắc cho mỗi loại thông báo
     */
    public String getColorCode() {
        return switch (type) {
            case ORDER -> "#4CAF50";    // Green
            case FAVORITE -> "#E91E63";  // Pink
            case COUPON -> "#FF9800";    // Orange
            case PRODUCT -> "#2196F3";   // Blue
            case SYSTEM -> "#9E9E9E";    // Gray
        };
    }
}
