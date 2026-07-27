package com.proj.webprojrct.luckywheel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LuckyWheelInfoResponse {
    private Boolean hasFreeSpinToday; // Còn lượt quay không
    private Integer spinCost; // Chi phí quay (coin) - luôn = 0
    private Integer currentPoints; // Số coin hiện tại
    private Long todaySpins; // Số lần đã quay hôm nay
    private Integer totalCoinsWon; // Tổng coin đã thắng
    private List<RewardItem> rewards; // Danh sách phần thưởng
    private Long productsViewedToday; // Số sản phẩm đã xem hôm nay
    private Integer requiredProductViews; // Số sản phẩm cần xem để có lượt quay (=3)
    private Boolean wheelEnabled; // Vòng quay có được bật không
    
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
        private Integer weight; // Trọng số
        private BigDecimal probability; // Xác suất (%)
    }
}
