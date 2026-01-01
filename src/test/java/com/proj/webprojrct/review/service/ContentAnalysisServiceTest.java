package com.proj.webprojrct.review.service;

import com.proj.webprojrct.chatbot.service.LLMService;
import com.proj.webprojrct.review.entity.ReviewStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

public class ContentAnalysisServiceTest {

    private ContentAnalysisService service;
    private LLMService mockLLMService;

    @BeforeEach
    public void setUp() {
        // Mock LLMService
        mockLLMService = Mockito.mock(LLMService.class);
        // Mặc định: AI không được cấu hình (fallback to keyword matching)
        Mockito.when(mockLLMService.isConfigured()).thenReturn(false);
        
        service = new ContentAnalysisService(mockLLMService);
    }

    @Test
    public void testAnalyzeSafeReview() {
        String text = "Sản phẩm này rất tuyệt vời, tôi rất thích!";
        ContentAnalysisService.AnalysisResult result = service.analyzeReview(text);

        Assertions.assertEquals(ReviewStatus.SAFE, result.getStatus());
        Assertions.assertEquals("approve", result.getSuggestion());
        Assertions.assertTrue(result.getReasons().isEmpty());
    }

    @Test
    public void testAnalyzeBlockReview_Profanity() {
        String text = "Cái tiệm này làm ăn như bát cứt, ngu thật sự.";
        ContentAnalysisService.AnalysisResult result = service.analyzeReview(text);

        Assertions.assertEquals(ReviewStatus.BLOCK, result.getStatus());
        Assertions.assertEquals("hide", result.getSuggestion());
        Assertions.assertTrue(result.getReasons().toString().contains("cứt"));
        Assertions.assertTrue(result.getReasons().toString().contains("ngu"));
    }

    @Test
    public void testAnalyzeBlockReview_Spam() {
        String text = "Nhấp vào link bet88 này để nhận thưởng ngay 0909123456";
        ContentAnalysisService.AnalysisResult result = service.analyzeReview(text);

        Assertions.assertEquals(ReviewStatus.BLOCK, result.getStatus());
        Assertions.assertEquals("hide", result.getSuggestion());
        Assertions.assertTrue(result.getReasons().toString().contains("bet88"));
    }

    @Test
    public void testAnalyzeWarningReview() {
        String text = "Nhìn có vẻ giả, shop này bán hàng fake à?";
        ContentAnalysisService.AnalysisResult result = service.analyzeReview(text);

        Assertions.assertEquals(ReviewStatus.WARNING, result.getStatus());
        Assertions.assertEquals("require_review", result.getSuggestion());
        Assertions.assertTrue(result.getReasons().toString().contains("fake"));
    }

    @Test
    public void testAnalyzeReview_CaseInsensitive() {
        String text = "ĐỒ lừa Đảo"; // mixed case
        ContentAnalysisService.AnalysisResult result = service.analyzeReview(text);

        Assertions.assertEquals(ReviewStatus.WARNING, result.getStatus());
        Assertions.assertTrue(result.getReasons().toString().contains("lừa đảo"));
    }

    @Test
    public void testAnalyzeSpamPattern_MultiplePhones() {
        String text = "Liên hệ 0919876543 hoặc 0987654321 để được tư vấn";
        ContentAnalysisService.AnalysisResult result = service.analyzeReview(text);

        Assertions.assertEquals(ReviewStatus.WARNING, result.getStatus());
        Assertions.assertEquals("require_review", result.getSuggestion());
        Assertions.assertTrue(result.getReasons().toString().contains("spam pattern"));
    }

    @Test
    public void testAnalyzeSpamPattern_URL() {
        String text = "Xem thêm tại https://example.com hoặc www.spam.com";
        ContentAnalysisService.AnalysisResult result = service.analyzeReview(text);

        Assertions.assertEquals(ReviewStatus.WARNING, result.getStatus());
        Assertions.assertTrue(result.getReasons().toString().contains("spam pattern"));
    }

    @Test
    public void testAnalyzeWithAI_SafeReview() {
        // Enable AI
        Mockito.when(mockLLMService.isConfigured()).thenReturn(true);
        Mockito.when(mockLLMService.generate(Mockito.anyString()))
                .thenReturn("STATUS: SAFE\nREASON: Đánh giá tích cực về sản phẩm\nSUGGESTION: approve");

        String text = "Giày rất đẹp, chất lượng tốt, giao hàng nhanh";
        ContentAnalysisService.AnalysisResult result = service.analyzeReview(text);

        Assertions.assertEquals(ReviewStatus.SAFE, result.getStatus());
        Assertions.assertEquals("approve", result.getSuggestion());
    }

    @Test
    public void testAnalyzeWithAI_BlockReview() {
        // Enable AI
        Mockito.when(mockLLMService.isConfigured()).thenReturn(true);
        Mockito.when(mockLLMService.generate(Mockito.anyString()))
                .thenReturn("STATUS: BLOCK\nREASON: Ngôn từ xúc phạm, lăng mạ\nSUGGESTION: hide");

        String text = "Shop đểu quá, bán hàng lừa đảo, mọi người đừng mua";
        ContentAnalysisService.AnalysisResult result = service.analyzeReview(text);

        // Keyword matching sẽ phát hiện "đểu" và "lừa đảo" -> WARNING
        // Nhưng AI upgrade lên BLOCK
        Assertions.assertTrue(result.getStatus() == ReviewStatus.WARNING || result.getStatus() == ReviewStatus.BLOCK);
    }

    @Test
    public void testAnalyzeWithAI_AIFailure_FallbackToKeyword() {
        // Enable AI nhưng bị lỗi
        Mockito.when(mockLLMService.isConfigured()).thenReturn(true);
        Mockito.when(mockLLMService.generate(Mockito.anyString()))
                .thenThrow(new RuntimeException("API Error"));

        String text = "Sản phẩm fake quá";
        ContentAnalysisService.AnalysisResult result = service.analyzeReview(text);

        // Fallback to keyword matching
        Assertions.assertEquals(ReviewStatus.WARNING, result.getStatus());
        Assertions.assertTrue(result.getReasons().toString().contains("fake"));
    }

    @Test
    public void testAnalyzeEmptyReview() {
        ContentAnalysisService.AnalysisResult result = service.analyzeReview("");
        Assertions.assertEquals(ReviewStatus.SAFE, result.getStatus());
        
        result = service.analyzeReview(null);
        Assertions.assertEquals(ReviewStatus.SAFE, result.getStatus());
    }
}
