package com.proj.webprojrct.chatbot.controller;

import com.proj.webprojrct.chatbot.dto.ChatRequest;
import com.proj.webprojrct.chatbot.dto.ChatResponse;
import com.proj.webprojrct.chatbot.entity.ChatConversation;
import com.proj.webprojrct.chatbot.entity.ChatMessage;
import com.proj.webprojrct.chatbot.service.DataSeedService;
import com.proj.webprojrct.chatbot.service.DocumentIngestionService;
import com.proj.webprojrct.chatbot.service.RAGService;
import com.proj.webprojrct.common.config.ApiMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST API Controller for RAG Chatbot
 * Enabled - pgvector extension is now configured
 */
@RestController
@RequestMapping("/api/v1/chat")
@Slf4j
@RequiredArgsConstructor
public class ChatController {

    private final RAGService ragService;
    private final DocumentIngestionService documentIngestionService;
    private final DataSeedService dataSeedService;

    /**
     * Chat endpoint - main interface for users
     */
    @PostMapping
    @ApiMessage("Send message to chatbot")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        // Generate session ID if not provided
        if (request.getSessionId() == null || request.getSessionId().isEmpty()) {
            request.setSessionId(UUID.randomUUID().toString());
        }

        log.info("💬 Chat request - Session: {} | Message: {}",
                request.getSessionId(), request.getMessage());

        ChatResponse response = ragService.chat(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get conversation history
     */
    @GetMapping("/conversations/{sessionId}")
    @ApiMessage("Get conversation messages")
    public ResponseEntity<List<ChatMessage>> getConversation(@PathVariable String sessionId) {
        List<ChatMessage> messages = ragService.getConversationMessages(sessionId);
        return ResponseEntity.ok(messages);
    }

    /**
     * Get user conversations (requires authentication)
     */
    @GetMapping("/user/{userId}/conversations")
    @ApiMessage("Get user conversations")
    public ResponseEntity<List<ChatConversation>> getUserConversations(@PathVariable Long userId) {
        List<ChatConversation> conversations = ragService.getUserConversations(userId);
        return ResponseEntity.ok(conversations);
    }

    /**
     * Upload PDF to knowledge base (admin only)
     */
    @PostMapping("/admin/upload-pdf")
    @ApiMessage("Upload PDF to knowledge base")
    public ResponseEntity<Map<String, Object>> uploadPDF(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "sourceType", defaultValue = "pdf") String sourceType,
            @RequestParam(value = "category", required = false) String category) {
        log.info("📄 Uploading PDF: {} | Size: {} bytes", file.getOriginalFilename(), file.getSize());

        Map<String, Object> metadata = new HashMap<>();
        if (category != null) {
            metadata.put("category", category);
        }

        int chunksStored = documentIngestionService.ingestPDF(file, sourceType, metadata);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("filename", file.getOriginalFilename());
        response.put("chunks_stored", chunksStored);
        response.put("message", "PDF uploaded and processed successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Add text to knowledge base (admin only)
     */
    @PostMapping("/admin/add-text")
    @ApiMessage("Add text to knowledge base")
    public ResponseEntity<Map<String, Object>> addText(@RequestBody Map<String, Object> request) {
        String text = (String) request.get("text");
        String source = (String) request.get("source");
        String sourceType = (String) request.getOrDefault("sourceType", "text");

        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = (Map<String, Object>) request.getOrDefault("metadata", new HashMap<>());

        log.info("📝 Adding text - Source: {} | Type: {}", source, sourceType);

        int chunksStored = documentIngestionService.ingestText(text, source, sourceType, metadata);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("source", source);
        response.put("chunks_stored", chunksStored);
        response.put("message", "Text added to knowledge base successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Delete documents by source (admin only)
     */
    @DeleteMapping("/admin/documents/{source}")
    @ApiMessage("Delete documents by source")
    public ResponseEntity<Map<String, Object>> deleteBySource(@PathVariable String source) {
        log.info("🗑️ Deleting documents from source: {}", source);

        documentIngestionService.deleteBySource(source);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Documents deleted successfully");

        return ResponseEntity.ok(response);
    }

    /**
     * Manual Trigger for Product Seeding (Admin)
     */
    @PostMapping("/admin/seed-products")
    @ApiMessage("Trigger manual product seeding")
    public ResponseEntity<Map<String, Object>> seedProductsManual() {
        log.info("🌱 Manual seeding triggered");
        Map<String, Object> result = dataSeedService.seedProducts();
        result.put("success", true);
        result.put("message", "Manual seeding completed");
        return ResponseEntity.ok(result);
    }

    /**
     * Manual Trigger for FAQs & Policies Seeding (Admin)
     */
    @PostMapping("/admin/seed-knowledge")
    @ApiMessage("Trigger manual FAQs and policies seeding")
    public ResponseEntity<Map<String, Object>> seedKnowledgeManual() {
        log.info("📚 Manual knowledge seeding triggered");
        Map<String, Object> result = new HashMap<>();
        
        try {
            dataSeedService.seedFAQs();
            dataSeedService.seedPolicies();
            
            result.put("success", true);
            result.put("message", "Knowledge base seeded successfully");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ Error seeding knowledge: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * Reset and Reseed All Data (Admin)
     */
    @PostMapping("/admin/reseed-all")
    @ApiMessage("Reset and reseed all vector data")
    public ResponseEntity<Map<String, Object>> reseedAll() {
        log.info("🔄 Resetting and reseeding all vector data...");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Delete all existing vector data
            log.info("🗑️ Deleting existing vector documents...");
            documentIngestionService.deleteBySourceType("product");
            documentIngestionService.deleteBySourceType("faq");
            documentIngestionService.deleteBySourceType("policy");
            
            // Reseed all data
            log.info("🌱 Reseeding data...");
            Map<String, Object> productResult = dataSeedService.seedProducts();
            
            result.put("success", true);
            result.put("message", "All data reset and reseeded successfully");
            result.put("product_result", productResult);
            result.put("products_indexed", productResult.get("indexed"));
            result.put("products_errors", productResult.get("errors"));
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("❌ Error during reseed: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    /**
     * Get knowledge base statistics (admin only)
     */
    @GetMapping("/admin/stats")
    @ApiMessage("Get knowledge base statistics")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total_documents", documentIngestionService.getDocumentCount(null));
        stats.put("pdf_documents", documentIngestionService.getDocumentCount("pdf"));
        stats.put("product_documents", documentIngestionService.getDocumentCount("product"));
        stats.put("faq_documents", documentIngestionService.getDocumentCount("faq"));

        return ResponseEntity.ok(stats);
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    @ApiMessage("Chatbot health check")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("message", "Chatbot service is running");

        return ResponseEntity.ok(health);
    }
}
