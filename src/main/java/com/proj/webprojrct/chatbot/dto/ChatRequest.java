package com.proj.webprojrct.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    private String message;
    private String sessionId;
    private Long userId; // Optional - for logged-in users
    private Integer topK; // Number of documents to retrieve (default: 5)
    private String sourceType; // Filter by source type (optional)
}
