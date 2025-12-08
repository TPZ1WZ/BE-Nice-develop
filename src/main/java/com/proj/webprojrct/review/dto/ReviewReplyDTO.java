package com.proj.webprojrct.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewReplyDTO {
    private Long id;
    private Long reviewId;
    private Long userId;
    private String userName;
    private String comment;
    private LocalDateTime createdAt;
    private boolean isAdminReply;
}