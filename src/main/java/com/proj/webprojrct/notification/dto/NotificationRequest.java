package com.proj.webprojrct.notification.dto;

import lombok.*;

import java.util.Map;

/**
 * NotificationRequest - DTO để tạo thông báo mới
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequest {
    private String type;  // ORDER, FAVORITE, COUPON, PRODUCT, SYSTEM
    private String title;
    private String message;
    private Map<String, Object> data;
}
