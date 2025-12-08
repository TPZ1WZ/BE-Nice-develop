package com.proj.webprojrct.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewSummaryResponse {
    private Long productId;
    private String productName;
    
    @DecimalMin(value = "0.0", message = "Đánh giá trung bình phải từ 0.0")
    @DecimalMax(value = "5.0", message = "Đánh giá trung bình phải từ 0.0 đến 5.0")
    private Double averageRating;
    
    @Min(value = 0, message = "Tổng số đánh giá phải từ 0 trở lên")
    private Integer totalReviews;
    
    @Builder.Default
    private Map<Integer, Integer> ratingDistribution = new HashMap<>(); // Key: rating (1-5), Value: count
    
    @Min(value = 0, message = "Tổng số đánh giá có ảnh phải từ 0 trở lên")
    private Integer totalWithImages;
    
    @Min(value = 0, message = "Tổng số mua hàng đã xác thực phải từ 0 trở lên")
    private Integer verifiedPurchases;
}