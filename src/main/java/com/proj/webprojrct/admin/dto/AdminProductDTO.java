package com.proj.webprojrct.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO cho Admin Product API - match với AdminProduct.java của Android app
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminProductDTO {
    private Long id;
    private String name;
    private String sku;
    private String category;
    private Double price;
    private Double salePrice;
    private Integer stock;
    private String status; // "active" or "inactive"
    private String image;
    private String description;
    private java.util.List<String> sizes;
    private LocalDateTime createdAt;
}
