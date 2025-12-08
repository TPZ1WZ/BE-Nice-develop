package com.proj.webprojrct.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReplyResponse {
    private Long id;
    
    @NotNull(message = "ID đánh giá không được để trống")
    private Long reviewId;
    
    @NotNull(message = "ID người dùng không được để trống")
    private Long userId;
    
    private String userName;
    
    private Boolean isAdmin;
    
    @NotBlank(message = "Nội dung trả lời không được để trống")
    @Size(max = 1000, message = "Nội dung trả lời không được vượt quá 1000 ký tự")
    private String content;
    
    private Boolean isHidden;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}