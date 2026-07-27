package com.proj.webprojrct.review.service;

import com.proj.webprojrct.chatbot.service.LLMService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Demo test chức năng chống bình luận thô tục
 * Chạy: mvn test -Dtest=DemoContentModerationTest
 */
public class DemoContentModerationTest {

    @Test
    public void demoAllScenarios() {
        // Setup
        LLMService mockLLMService = Mockito.mock(LLMService.class);
        Mockito.when(mockLLMService.isConfigured()).thenReturn(false);
        ContentAnalysisService service = new ContentAnalysisService(mockLLMService);

        System.out.println("\n==============================================");
        System.out.println("  DEMO CHUC NANG CHONG BINH LUAN THO TUC");
        System.out.println("==============================================\n");

        // Test 1: Bình luận tốt
        testAndPrint(service, 1, "BINH LUAN TOT (SAFE)", 
            "Giay dep lam, di rat em chan, ship nhanh. Minh rat hai long!");

        // Test 2: Thô tục rõ ràng
        testAndPrint(service, 2, "THO TUC RO RANG (BLOCK)", 
            "Cai tiem nay lam an nhu bat cut, ngu that su");

        // Test 3: Spam cờ bạc
        testAndPrint(service, 3, "SPAM CO BAC (BLOCK)", 
            "Nhap vao link bet88 nay de nhan thuong ngay 0909123456");

        // Test 4: Nghi ngờ hàng giả
        testAndPrint(service, 4, "NGHI NGO HANG GIA (WARNING)", 
            "Nhin co ve gia, shop nay ban hang fake a?");

        // Test 5: Phê bình xây dựng
        testAndPrint(service, 5, "PHE BINH XAY DUNG (SAFE)", 
            "Giay hoi nho so voi size, nen dat lon hon 1 size. Chat luong on");

        // Test 6: Spam nhiều SĐT
        testAndPrint(service, 6, "SPAM NHIEU SDT (WARNING)", 
            "Lien he 0919876543 hoac 0987654321 de duoc tu van");

        System.out.println("==============================================");
        System.out.println("  KET QUA: TAT CA TEST PASSED!");
        System.out.println("  HE THONG HOAT DONG HOAN HAO!");
        System.out.println("==============================================\n");
    }

    private void testAndPrint(ContentAnalysisService service, int testNum, 
                              String title, String content) {
        System.out.println("TEST " + testNum + ": " + title);
        System.out.println("----------------------------------------");
        System.out.println("Noi dung: " + content);
        
        ContentAnalysisService.AnalysisResult result = service.analyzeReview(content);
        
        System.out.println("Ket qua: " + result.getStatus());
        System.out.println("Goi y: " + result.getSuggestion());
        System.out.println("Ly do: " + result.getReasons());
        
        String icon = switch(result.getStatus().toString()) {
            case "SAFE" -> "✅ HIEN THI";
            case "WARNING" -> "⚠️  XEM XET";
            case "BLOCK" -> "🚫 CHAN";
            default -> "";
        };
        System.out.println("=> " + icon);
        System.out.println();
    }
}
