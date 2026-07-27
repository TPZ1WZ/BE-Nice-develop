package com.proj.webprojrct.loyalty.dto;

import lombok.*;

/**
 * DTO cho response khi checkin thành công
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckinResponse {
    
    private Boolean success;
    private String message;
    private Integer rewardAmount; // Số coin nhận được
    private Integer currentStreak; // Chuỗi ngày liên tiếp mới
    private Integer totalPoints; // Tổng điểm sau khi checkin
    private Long transactionId; // ID của transaction vừa tạo
}
