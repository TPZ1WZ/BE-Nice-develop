package com.proj.webprojrct.luckywheel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpinRequest {
    private Boolean useFreeSpin; // true = dùng lượt free, false = trả coin
}
