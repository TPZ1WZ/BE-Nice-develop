package com.proj.webprojrct.luckywheel.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO để cập nhật xác suất nhiều phần thưởng cùng lúc
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PrizeProbabilityUpdate {
    private Long prizeId;
    private Double probability; // 0.0 - 1.0 (hoặc 0 - 100%)
}
