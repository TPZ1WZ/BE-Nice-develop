package com.proj.webprojrct.product.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailDto {
    private Long id;
    private String name;
    private String slug;
    private String subTitle;
    private String description;
    private Double price;
    private Integer stock;
    private List<String> images;
    private List<Long> categoryId;
    private Long reviewId;
    private Boolean isFavorite;
    // Thêm các trường chi tiết khác nếu cần
}