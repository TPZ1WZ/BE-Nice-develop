package com.proj.webprojrct.luckywheel.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Thống kê vòng quay cho Admin
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LuckyWheelStatistics {
    private Long totalSpins;
    private Long spinsToday;
    private Long spinsThisWeek;
    private Long uniqueUsers;
    private Long totalPrizesWon;
    private String mostPopularPrize;
    private Long mostPopularPrizeCount;
    private String topSpinner;
    private Long topSpinnerCount;
}
