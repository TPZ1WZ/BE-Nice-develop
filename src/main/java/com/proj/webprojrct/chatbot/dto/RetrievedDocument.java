package com.proj.webprojrct.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetrievedDocument {
    private Long id;
    private String content;
    private Map<String, Object> metadata;
    private String source;
    private String sourceType;
    private Float similarity;
}
