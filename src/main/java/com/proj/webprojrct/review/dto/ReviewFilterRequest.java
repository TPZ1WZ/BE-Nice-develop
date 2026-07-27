package com.proj.webprojrct.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewFilterRequest {
    private Long productId;
    private Integer rating;
    private String sortBy; // newest, oldest, rating_high, rating_low
    private int page = 0;
    private int pageSize = 10;
    private Boolean approved;
    private String reviewStatus; // SAFE, WARNING, BLOCK
}