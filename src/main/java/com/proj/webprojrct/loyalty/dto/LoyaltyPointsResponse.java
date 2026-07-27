package com.proj.webprojrct.loyalty.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO cho thông tin loyalty points của user
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyPointsResponse {
    
    private Integer currentPoints; // Số điểm hiện tại
    private Integer totalEarned; // Tổng điểm đã nhận
    private Integer totalSpent; // Tổng điểm đã tiêu
    private Integer currentStreak; // Chuỗi checkin hiện tại
    private Long totalCheckins; // Tổng số lần checkin
    
    // Thông tin về coin sắp hết hạn
    private Integer expiringCoins; // Số coin sẽ hết hạn sớm nhất
    private LocalDateTime expiryDate; // Ngày hết hạn gần nhất
}
