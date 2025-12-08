package com.proj.webprojrct.review.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateReviewRequest {
    private Integer rating;
    private String comment;
    private String title;
    private List<String> images;
}