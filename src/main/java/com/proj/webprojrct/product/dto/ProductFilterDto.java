package com.proj.webprojrct.product.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilterDto {
    private String name;
    private Double minPrice;
    private Double maxPrice;
    private List<Long> categoryIds;
    
    // Thêm bộ lọc theo size
    private List<String> sizes; // Danh sách size cần lọc (VD: ["38", "39", "40"])
    
    private String sortBy; // name, price, createdAt
    private String sortDirection; // ASC, DESC
    private Integer page;
    private Integer pageSize;
}