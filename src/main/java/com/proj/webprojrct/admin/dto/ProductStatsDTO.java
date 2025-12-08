package com.proj.webprojrct.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho Product Statistics - match với ProductStats.java của Android app
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductStatsDTO {
    private Integer total;
    private Integer outOfStock;
    private Integer lowStock;
}
