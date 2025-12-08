package com.proj.webprojrct.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductManagementResponse {
    
    private Long id;
    private String name;
    private String slug;
    private String subTitle;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String status; // ACTIVE, INACTIVE, OUT_OF_STOCK
    private List<String> images;
    private String categoryName;
    private Integer totalSold; // Tổng số đã bán
    private Double averageRating;
    private Integer reviewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // === THỐNG KÊ NHANH ===
    private BigDecimal totalRevenue; // Doanh thu từ sản phẩm này
    private Integer viewCount; // Số lượt xem (nếu có tracking)
    private String stockStatus; // LOW_STOCK, OUT_OF_STOCK, IN_STOCK
}