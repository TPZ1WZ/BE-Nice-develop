package com.proj.webprojrct.luckywheel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LuckyWheelInfoResponse {
    private Boolean hasFreeSpinToday; // Còn lượt quay miễn phí không
    private Integer spinCost; // Chi phí quay (coin)
    private Integer currentPoints; // Số coin hiện tại
    private Long todaySpins; // Số lần đã quay hôm nay
    private Integer totalCoinsWon; // Tổng coin đã thắng
    private List<RewardItem> rewards; // Danh sách phần thưởng
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RewardItem {
        private Long id;
        private Integer position;
        private String rewardType;
        private Integer coinAmount;
        private String label;
        private String iconName;
    }
}
