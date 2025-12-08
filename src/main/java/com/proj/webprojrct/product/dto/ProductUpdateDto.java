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
public class ProductUpdateDto {
    private String name;
    private String slug;
    private String subTitle;
    private String description;
    private Double price;
    private Integer stock;
    private List<String> images;
    private Long categoryId;
    private String color;
    private String size;
}