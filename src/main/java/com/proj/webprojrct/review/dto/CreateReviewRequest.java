package com.proj.webprojrct.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateReviewRequest {
    private Long productId;
    private Long orderId;
    private Integer rating; // 1-5 stars
    private String comment;
    private String title;
    private List<String> images;
}