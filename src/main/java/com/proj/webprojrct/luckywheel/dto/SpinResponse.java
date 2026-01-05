package com.proj.webprojrct.luckywheel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpinResponse {
    private Boolean success;
    private String message;
    private Integer rewardPosition; // Vị trí trúng thưởng (0-7)
    private String rewardType; // COIN, COUPON, NOTHING
    private Integer coinAmount; // Số coin nhận được
    private Integer totalPoints; // Tổng coin sau khi quay
    private Boolean hasFreeSpinLeft; // Còn lượt free không
}
