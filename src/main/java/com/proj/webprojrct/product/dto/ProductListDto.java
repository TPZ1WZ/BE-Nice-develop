package com.proj.webprojrct.product.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductListDto {
    private Long id;
    private String name;
    private Double price;
    private String thumbnail;
}