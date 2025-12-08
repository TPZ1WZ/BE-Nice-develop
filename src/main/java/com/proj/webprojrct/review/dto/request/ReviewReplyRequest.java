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
public class ReviewReplyRequest {
    @NotNull(message = "ID đánh giá không được để trống")
    private Long reviewId;
    
    @NotBlank(message = "Nội dung trả lời không được để trống")
    @Size(max = 1000, message = "Nội dung trả lời không được vượt quá 1000 ký tự")
    private String content;
    
    private Long parentReplyId; // Cho reply của reply
}