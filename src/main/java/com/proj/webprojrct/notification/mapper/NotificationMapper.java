package com.proj.webprojrct.notification.mapper;

import com.proj.webprojrct.notification.dto.NotificationResponse;
import com.proj.webprojrct.notification.entity.Notification;
import org.springframework.stereotype.Component;

/**
 * NotificationMapper - Chuyển đổi giữa Entity và DTO
 */
@Component
public class NotificationMapper {

    /**
     * Chuyển từ Entity sang Response DTO
     */
    public NotificationResponse toResponse(Notification notification) {
        if (notification == null) {
            return null;
        }

        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .data(notification.getData())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
}
