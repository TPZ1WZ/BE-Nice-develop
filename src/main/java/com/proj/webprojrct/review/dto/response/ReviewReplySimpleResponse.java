package com.proj.webprojrct.review.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReplySimpleResponse {
    private Long id;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
    private String userName;
    private String userRole; // ADMIN, STAFF, CUSTOMER
    private Boolean isAdminReply;
}