package com.proj.webprojrct.luckywheel.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request để cập nhật xác suất nhiều phần thưởng
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BatchProbabilityRequest {
    private List<PrizeProbabilityUpdate> prizes;
}
