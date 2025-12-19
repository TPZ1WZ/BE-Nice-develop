package com.proj.webprojrct.favorite.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteResponseDto {
    private Long id;
    private Long productId;
    private String name;
    private String slug;
    private String subTitle;
    private Double price;
    private Integer stock;
    private List<String> images;
    private String categoryName;
    private LocalDateTime addedAt;
}
