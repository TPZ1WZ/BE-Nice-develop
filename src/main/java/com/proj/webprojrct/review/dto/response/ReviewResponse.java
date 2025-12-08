package com.proj.webprojrct.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;
    private String title; // THÊM title từ request của bạn
    private Integer rating;
    private String comment;
    private Boolean isApproved; // true = approved, false = pending
    private Boolean isHidden; // true = hidden/rejected, false = visible
    private Boolean isVerifiedPurchase;
    private Integer helpfulCount; // Helpful count để sắp xếp
    private Integer likes;
    private Integer dislikes;
    private Integer reportedCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // User info
    private Long userId;
    private String userName;
    private String userAvatar;
    
    // Product info
    private Long productId;
    private String productName;
    private String productThumbnail;
    
    // Images
    private List<String> imageUrls;
    
    // Replies
    private List<ReviewReplySimpleResponse> replies;
    
    
    // Cờ cho phép chỉnh sửa (nếu là chủ sở hữu)
    private Boolean canEdit;

    // Status text để hiển thị (tùy chọn)
    public String getStatusText() {
        if (Boolean.TRUE.equals(isHidden)) {
            return "HIDDEN";
        } else if (Boolean.TRUE.equals(isApproved)) {
            return "APPROVED";
        } else {
            return "PENDING";
        }
    }
}