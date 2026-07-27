package com.proj.webprojrct.review.service;

import com.proj.webprojrct.chatbot.service.LLMService;
import com.proj.webprojrct.review.entity.ReviewStatus;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentAnalysisService {

    private final LLMService llmService;

    private static final Set<String> BLOCK_KEYWORDS = Set.of(
            "chửi", "đụ", "mẹ mày", "ngu", "chó", "cứt", "đĩ", "fuck", "shit", "bitch",
            "nhấp vào link", "gọi ngay", "0909", "cờ bạc", "bet88", "dmm", "dm", "vãi", "vcl", "địt");

    private static final Set<String> WARNING_KEYWORDS = Set.of(
            "lừa đảo", "fake", "đểu", "giả", "kém chất lượng", "treo đầu dê",
            "hoàn tiền", "liên hệ zalo", "scam", "rác");

    @Data
    @Builder
    public static class AnalysisResult {
        private ReviewStatus status;
        private List<String> reasons;
        private String suggestion;
    }

    /**
     * Phân tích review bằng cả keyword matching VÀ AI (Gemini)
     * - Fast path: keyword matching (nhanh, chặn rõ ràng)
     * - Smart path: AI analysis (chính xác, hiểu ngữ cảnh)
     */
    public AnalysisResult analyzeReview(String text) {
        if (text == null || text.trim().isEmpty()) {
            return AnalysisResult.builder()
                    .status(ReviewStatus.SAFE)
                    .reasons(List.of())
                    .suggestion("approve")
                    .build();
        }

        // STEP 1: Fast keyword check (chặn rõ ràng trước)
        AnalysisResult keywordResult = analyzeByKeywords(text);
        
        // Nếu keyword matching đã BLOCK → không cần gọi AI
        if (keywordResult.getStatus() == ReviewStatus.BLOCK) {
            log.info("🚫 Review blocked by keywords: {}", keywordResult.getReasons());
            return keywordResult;
        }

        // STEP 2: AI analysis (chính xác hơn, hiểu ngữ cảnh)
        if (llmService.isConfigured()) {
            try {
                AnalysisResult aiResult = analyzeByAI(text);
                
                // AI có quyền override WARNING hoặc phát hiện thêm vấn đề
                if (aiResult.getStatus().ordinal() > keywordResult.getStatus().ordinal()) {
                    log.info("🤖 AI detected higher risk: {} -> {}", 
                            keywordResult.getStatus(), aiResult.getStatus());
                    
                    // Merge reasons từ cả keyword và AI
                    List<String> mergedReasons = new ArrayList<>(keywordResult.getReasons());
                    mergedReasons.addAll(aiResult.getReasons());
                    
                    return AnalysisResult.builder()
                            .status(aiResult.getStatus())
                            .reasons(mergedReasons)
                            .suggestion(aiResult.getSuggestion())
                            .build();
                }
            } catch (Exception e) {
                log.error("❌ AI analysis failed, fallback to keyword only: {}", e.getMessage());
            }
        } else {
            log.debug("⚠️ AI not configured, using keyword matching only");
        }

        return keywordResult;
    }

    /**
     * Phân tích bằng keyword matching (fast path)
     */
    private AnalysisResult analyzeByKeywords(String text) {
        String lowerText = text.toLowerCase();
        List<String> reasons = new ArrayList<>();
        ReviewStatus status = ReviewStatus.SAFE;
        String suggestion = "approve";

        // Check BLOCK keywords
        List<String> foundBlockIds = BLOCK_KEYWORDS.stream()
                .filter(lowerText::contains)
                .collect(Collectors.toList());

        if (!foundBlockIds.isEmpty()) {
            status = ReviewStatus.BLOCK;
            suggestion = "hide";
            reasons.add("Chứa từ khóa cấm/tục tữu: " + String.join(", ", foundBlockIds));
        }

        // Check WARNING keywords if not already blocked
        if (status == ReviewStatus.SAFE) {
            List<String> foundWarningIds = WARNING_KEYWORDS.stream()
                    .filter(lowerText::contains)
                    .collect(Collectors.toList());

            if (!foundWarningIds.isEmpty()) {
                status = ReviewStatus.WARNING;
                suggestion = "require_review";
                reasons.add("Nội dung nghi ngờ/tiêu cực: " + String.join(", ", foundWarningIds));
            }
        }

        // Check spam patterns
        if (status == ReviewStatus.SAFE && detectSpamPatterns(text)) {
            status = ReviewStatus.WARNING;
            suggestion = "require_review";
            reasons.add("Phát hiện spam pattern");
        }

        return AnalysisResult.builder()
                .status(status)
                .reasons(reasons)
                .suggestion(suggestion)
                .build();
    }

    /**
     * Phát hiện spam patterns (nhiều số điện thoại, link, v.v.)
     */
    private boolean detectSpamPatterns(String text) {
        // Pattern: Nhiều số điện thoại
        Pattern phonePattern = Pattern.compile("(0\\d{9}|\\+84\\d{9})");
        Matcher phoneMatcher = phonePattern.matcher(text);
        int phoneCount = 0;
        while (phoneMatcher.find()) phoneCount++;
        if (phoneCount >= 2) return true;

        // Pattern: URLs
        Pattern urlPattern = Pattern.compile("(https?://|www\\.)");
        if (urlPattern.matcher(text).find()) return true;

        // Pattern: Quá nhiều ký tự lặp lại
        return text.matches(".*(.)(\\1{4,}).*");
    }

    /**
     * Phân tích bằng AI (Gemini) - smart path
     */
    private AnalysisResult analyzeByAI(String text) {
        String prompt = buildModerationPrompt(text);
        String aiResponse = llmService.generate(prompt);

        log.debug("🤖 AI Response: {}", aiResponse);

        // Parse AI response
        return parseAIResponse(aiResponse);
    }

    /**
     * Tạo prompt cho Gemini để phân tích review
     */
    private String buildModerationPrompt(String reviewText) {
        return """
                Bạn là một AI moderator cho hệ thống review sản phẩm giày Nike.
                Nhiệm vụ: Phân tích nội dung review và đánh giá mức độ rủi ro.
                
                TIÊU CHÍ ĐÁNH GIÁ (QUAN TRỌNG - Tuân thủ nghiêm ngặt):
                
                1. **BLOCK** - Chỉ chặn khi có:
                   - Từ ngữ thô tục, chửi bậy RÕ RÀNG (ví dụ: ngu, cứt, đụ, dm, vcl...)
                   - Xúc phạm trực tiếp, hate speech
                   - Spam quảng cáo có link hoặc số điện thoại spam
                   - Cờ bạc, đánh bạc
                
                2. **WARNING** - Cảnh báo (cho các trường hợp):
                   - Cáo buộc hàng giả, fake, lừa đảo (KHÔNG chặn ngay)
                   - Nội dung tiêu cực mạnh nhưng KHÔNG có từ chửi bậy
                   - Nghi ngờ spam nhưng không chắc chắn
                   - Review có dấu hiệu không trung thực
                
                3. **SAFE** - An toàn:
                   - Review bình thường, đánh giá trung thực
                   - Góp ý, phàn nàn mang tính xây dựng
                   - Có thể tiêu cực về sản phẩm nhưng không vi phạm
                
                LƯU Ý: Cáo buộc "fake", "giả", "lừa đảo" KHÔNG phải BLOCK, chỉ là WARNING!
                
                REVIEW CẦN PHÂN TÍCH:
                "%s"
                
                TRẢ LỜI THEO ĐỊNH DẠNG SAU (không thêm gì khác):
                STATUS: [SAFE/WARNING/BLOCK]
                REASON: [Lý do cụ thể bằng tiếng Việt]
                SUGGESTION: [approve/require_review/hide]
                """.formatted(reviewText);
    }

    /**
     * Parse response từ Gemini
     */
    private AnalysisResult parseAIResponse(String aiResponse) {
        try {
            ReviewStatus status = ReviewStatus.SAFE;
            String suggestion = "approve";
            List<String> reasons = new ArrayList<>();

            // Parse STATUS
            if (aiResponse.contains("STATUS:")) {
                String statusLine = extractLine(aiResponse, "STATUS:");
                if (statusLine.contains("BLOCK")) {
                    status = ReviewStatus.BLOCK;
                    suggestion = "hide";
                } else if (statusLine.contains("WARNING")) {
                    status = ReviewStatus.WARNING;
                    suggestion = "require_review";
                }
            }

            // Parse REASON
            if (aiResponse.contains("REASON:")) {
                String reason = extractLine(aiResponse, "REASON:");
                if (!reason.isEmpty()) {
                    reasons.add("AI: " + reason);
                }
            }

            // Parse SUGGESTION (optional override)
            if (aiResponse.contains("SUGGESTION:")) {
                String sugLine = extractLine(aiResponse, "SUGGESTION:");
                if (sugLine.contains("hide")) suggestion = "hide";
                else if (sugLine.contains("require_review")) suggestion = "require_review";
                else if (sugLine.contains("approve")) suggestion = "approve";
            }

            return AnalysisResult.builder()
                    .status(status)
                    .reasons(reasons)
                    .suggestion(suggestion)
                    .build();

        } catch (Exception e) {
            log.error("❌ Failed to parse AI response: {}", e.getMessage());
            // Fallback to SAFE
            return AnalysisResult.builder()
                    .status(ReviewStatus.SAFE)
                    .reasons(List.of("AI parsing failed, defaulting to safe"))
                    .suggestion("approve")
                    .build();
        }
    }

    /**
     * Extract line value after a label
     */
    private String extractLine(String text, String label) {
        int start = text.indexOf(label);
        if (start == -1) return "";
        
        start += label.length();
        int end = text.indexOf("\n", start);
        if (end == -1) end = text.length();
        
        return text.substring(start, end).trim();
    }
}
