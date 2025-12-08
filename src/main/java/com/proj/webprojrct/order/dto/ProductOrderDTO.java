package com.proj.webprojrct.order.dto;


import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductOrderDTO {
    private Long id;
    private String name;
    private String slug;
    private String subTitle;
    private String description;
    private Double price;
    private Integer stock;
    private boolean isDelete;
    private List<String> images;

    // Thông tin danh mục (có thể rút gọn nếu cần)
    private Long categoryId;
    private String categoryName;
}
