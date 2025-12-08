package com.proj.webprojrct.review.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewFilterRequest {
    private Long productId;
    
    @Min(value = 1, message = "Đánh giá tối thiểu phải từ 1 sao")
    @Max(value = 5, message = "Đánh giá tối thiểu phải từ 1 đến 5 sao")
    private Integer minRating;
    
    @Min(value = 1, message = "Đánh giá tối đa phải từ 1 sao")
    @Max(value = 5, message = "Đánh giá tối đa phải từ 1 đến 5 sao")
    private Integer maxRating;
    
    private Boolean hasImages;
    private Boolean isVerifiedPurchase;
    private Boolean isApproved;
    private Boolean isHidden;
    
    @Pattern(regexp = "newest|oldest|highest_rating|lowest_rating|most_helpful", 
             message = "Cách sắp xếp không hợp lệ")
    private String sortBy;
    
    @Min(value = 0, message = "Số trang phải từ 0 trở lên")
    @Builder.Default
    private Integer page = 0;
    
    @Min(value = 1, message = "Kích thước trang phải từ 1 trở lên")
    @Max(value = 100, message = "Kích thước trang không được vượt quá 100")
    @Builder.Default
    private Integer size = 10;
    
    // THÊM: Validation cho rating range
    @AssertTrue(message = "Đánh giá tối thiểu phải nhỏ hơn hoặc bằng đánh giá tối đa")
    public boolean isRatingRangeValid() {
        if (minRating != null && maxRating != null) {
            return minRating <= maxRating;
        }
        return true;
    }
    
    // THÊM: Helper method để build sort
    public String getSortField() {
        switch (sortBy) {
            case "highest_rating": return "rating";
            case "lowest_rating": return "rating";
            case "most_helpful": return "helpfulCount";
            case "oldest": return "createdAt";
            default: return "createdAt"; // newest
        }
    }
    
    public boolean isAscending() {
        return "lowest_rating".equals(sortBy) || "oldest".equals(sortBy);
    }
}