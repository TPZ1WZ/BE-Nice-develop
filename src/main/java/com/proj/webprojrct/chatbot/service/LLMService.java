package com.proj.webprojrct.chatbot.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for interacting with LLM (Google Gemini)
 * Free tier: 60 requests/minute, 1500 requests/day
 */
@Service
@Slf4j
public class LLMService {

    private final ChatLanguageModel chatModel;
    private final boolean isConfigured;

    public LLMService(@Value("${gemini.api.key:}") String apiKey) {
        ChatLanguageModel tempModel = null;
        boolean tempConfigured = false;

        if (apiKey == null || apiKey.trim().isEmpty() || apiKey.equals("your-gemini-api-key-here")) {
            log.warn("⚠️ Gemini API key not configured. LLM features will use fallback responses.");
            log.warn("⚠️ To enable LLM, add 'gemini.api.key' to application.properties");
            log.warn("⚠️ Get free API key at: https://makersuite.google.com/app/apikey");
        } else {
            try {
                log.info("🔧 Initializing Google Gemini Chat Model...");
                log.info("🔑 API Key configured (length: {})", apiKey.length());
                ChatLanguageModel model = GoogleAiGeminiChatModel.builder()
                        .apiKey(apiKey)
                        .modelName("gemini-2.5-flash") // Switching to 2.5-flash as requested by user
                        .temperature(0.7)
                        .maxOutputTokens(2048)
                        .build();
                log.info("✅ Google Gemini Chat Model initialized successfully");

                // Test the model with a simple call
                try {
                    String testResponse = model.generate("Say 'OK'");
                    log.info("✅ Gemini API test successful: {}", testResponse);
                } catch (Exception e) {
                    log.error("❌ Gemini API test failed: {}", e.getMessage());
                }
                // Always assign after test
                tempModel = model;
                tempConfigured = true;
            } catch (Exception e) {
                log.error("❌ Failed to initialize Gemini Chat Model: {}", e.getMessage(), e);
            }
        }

        chatModel = tempModel;
        isConfigured = tempConfigured;
    }

    /**
     * Generate response from LLM with context and retry logic
     */
    public String generate(String prompt) {
        if (!isConfigured) {
            return "❌ Lỗi Cấu hình: API Key chưa được cài đặt hoặc bị rỗng.";
        }

        int maxRetries = 3;
        int retryDelayMs = 2000; // 2 seconds
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("📤 Sending prompt to Gemini (attempt {}/{})...", attempt, maxRetries);
                String response = chatModel.generate(prompt);
                log.info("📥 Received response from Gemini");
                return response;
            } catch (Exception e) {
                String errorMsg = e.getMessage() != null ? e.getMessage() : e.toString();
                boolean isRateLimitError = errorMsg.contains("429") || 
                                          errorMsg.contains("rate limit") || 
                                          errorMsg.contains("Too Many Requests");
                
                log.error("❌ Error generating response (attempt {}/{}): {}", attempt, maxRetries, errorMsg);
                
                // Retry only for rate limit errors
                if (isRateLimitError && attempt < maxRetries) {
                    try {
                        log.warn("⏳ Rate limit detected, waiting {}ms before retry...", retryDelayMs);
                        Thread.sleep(retryDelayMs);
                        retryDelayMs *= 2; // Exponential backoff: 2s, 4s, 8s
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return "❌ Lỗi API Gemini: Retry bị gián đoạn";
                    }
                } else {
                    // Don't retry for other errors or if max retries reached
                    return "❌ Lỗi API Gemini: " + errorMsg;
                }
            }
        }
        
        return "❌ Lỗi API Gemini: Vượt quá số lần thử lại (rate limit)";
    }

    /**
     * Check if LLM is properly configured
     */
    public boolean isConfigured() {
        return isConfigured;
    }
}
