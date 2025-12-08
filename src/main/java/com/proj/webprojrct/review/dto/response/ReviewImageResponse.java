package com.proj.webprojrct.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewImageResponse {
    private Long id;
    
    @NotNull(message = "ID đánh giá không được để trống")
    private Long reviewId;
    
    @NotBlank(message = "URL ảnh không được để trống")
    private String imageUrl;
    
    private String caption;
    
    private Integer displayOrder;
}