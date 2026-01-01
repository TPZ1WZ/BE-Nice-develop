package com.proj.webprojrct.review.dto;

import com.proj.webprojrct.review.entity.ReviewStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDTO {
    private Long id;
    private Long productId;
    private String productName;
    private Long userId;
    private String userName;
    private Integer rating;
    private String comment;
    private String title;
    private List<String> images;
    private LocalDateTime createdAt;
    private boolean approved;

    // Admin/Moderation fields
    private ReviewStatus reviewStatus;
    private String adminNote;
    private String aiSuggestion;
    private List<String> aiReasons;
}