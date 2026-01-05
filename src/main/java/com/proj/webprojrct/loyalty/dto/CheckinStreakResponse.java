package com.proj.webprojrct.loyalty.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO cho thông tin checkin streak của user
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckinStreakResponse {
    
    private Integer currentStreak; // Số ngày liên tiếp hiện tại (0-7)
    private Boolean hasCheckedInToday; // Đã checkin hôm nay chưa
    private Integer todayReward; // Phần thưởng hôm nay (nếu chưa claim)
    private LocalDate lastCheckinDate; // Ngày checkin gần nhất
    private Long totalCheckins; // Tổng số lần đã checkin
    private List<DayRewardInfo> weeklyRewards; // Thông tin 7 ngày reward
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DayRewardInfo {
        private Integer dayNumber; // 1-7
        private Integer rewardAmount; // Số coin
        private Boolean isBonus; // Có phải ngày bonus không
        private CheckinStatus status; // PAST, TODAY, FUTURE
    }
    
    public enum CheckinStatus {
        PAST,    // Đã checkin (hoặc bỏ lỡ)
        TODAY,   // Hôm nay
        FUTURE   // Chưa tới
    }
}
