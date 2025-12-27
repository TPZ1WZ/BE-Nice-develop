package com.proj.webprojrct.chatbot.service;

import com.pgvector.PGvector;
import com.proj.webprojrct.chatbot.dto.ChatRequest;
import com.proj.webprojrct.chatbot.dto.ChatResponse;
import com.proj.webprojrct.chatbot.dto.RetrievedDocument;
import com.proj.webprojrct.chatbot.entity.ChatConversation;
import com.proj.webprojrct.chatbot.entity.ChatMessage;
import com.proj.webprojrct.chatbot.entity.VectorDocument;
import com.proj.webprojrct.chatbot.repository.ChatConversationRepository;
import com.proj.webprojrct.chatbot.repository.ChatMessageRepository;
import com.proj.webprojrct.chatbot.repository.VectorDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * RAG (Retrieval-Augmented Generation) Service
 * Combines vector search with LLM to provide context-aware responses
 * Enabled - pgvector extension is now configured
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RAGService {

    private final EmbeddingService embeddingService;
    private final LLMService llmService;
    private final VectorDocumentRepository vectorDocumentRepository;
    private final ChatConversationRepository chatConversationRepository;
    private final ChatMessageRepository chatMessageRepository;

    /**
     * Process chat request with RAG
     */
    @Transactional
    public ChatResponse chat(ChatRequest request) {
        log.info("💬 Processing chat request - Session: {} | Message: {}",
                request.getSessionId(), request.getMessage());

        try {
            // 1. Get or create conversation
            ChatConversation conversation = getOrCreateConversation(request.getSessionId(), request.getUserId());

            // 2. Save user message
            ChatMessage userMessage = ChatMessage.builder()
                    .conversation(conversation)
                    .role("user")
                    .content(request.getMessage())
                    .metadata(new HashMap<>())
                    .build();
            chatMessageRepository.save(userMessage);

            // 3. Try RAG approach first, fallback to direct LLM if embedding fails
            List<RetrievedDocument> relevantDocs = Collections.emptyList();
            String context = "";

            try {
                // Generate embedding for user query
                float[] queryEmbedding = embeddingService.embed(request.getMessage());

                // Retrieve relevant documents from vector store
                relevantDocs = retrieveRelevantDocuments(
                        queryEmbedding,
                        request.getTopK() != null ? request.getTopK() : 15,
                        request.getSourceType());

                log.info("📚 Retrieved {} relevant documents", relevantDocs.size());

                // Build context from retrieved documents
                context = buildContext(relevantDocs);
            } catch (Exception e) {
                log.warn("⚠️ RAG retrieval failed, using direct LLM: {}", e.getMessage());
            }

            // 6. Get conversation history
            String conversationHistory = getConversationHistory(conversation.getId(), 3);

            // 7. Build prompt with context (may be empty if RAG failed)
            String prompt = buildPrompt(request.getMessage(), context, conversationHistory);

            // 8. Generate response from LLM
            String assistantResponse = llmService.generate(prompt);

            // 9. Save assistant message
            ChatMessage assistantMessage = ChatMessage.builder()
                    .conversation(conversation)
                    .role("assistant")
                    .content(assistantResponse)
                    .metadata(Map.of(
                            "retrieved_docs_count", relevantDocs.size(),
                            "model", llmService.isConfigured() ? "gemini-1.5-flash" : "fallback"))
                    .build();
            chatMessageRepository.save(assistantMessage);

            log.info("✅ Chat response generated successfully");

            return ChatResponse.builder()
                    .message(assistantResponse)
                    .sessionId(request.getSessionId())
                    .sources(relevantDocs)
                    .build();

        } catch (Exception e) {
            log.error("❌ Error processing chat request: {}", e.getMessage(), e);
            return ChatResponse.builder()
                    .message("Xin lỗi, đã có lỗi xảy ra. Vui lòng thử lại sau.")
                    .sessionId(request.getSessionId())
                    .sources(Collections.emptyList())
                    .build();
        }
    }

    /**
     * Retrieve relevant documents using vector similarity search
     */
    private List<RetrievedDocument> retrieveRelevantDocuments(
            float[] queryEmbedding,
            int topK,
            String sourceType) {
        try {
            String vectorString = embeddingService.toVectorString(queryEmbedding);

            List<Object[]> results = vectorDocumentRepository.searchSimilar(
                    vectorString,
                    0.1f, // similarity threshold - very low to retrieve all potentially relevant results
                    topK,
                    sourceType);

            return results.stream()
                    .map(row -> RetrievedDocument.builder()
                            .id(((Number) row[0]).longValue())
                            .content((String) row[1])
                            .metadata(row[2] != null ? (Map<String, Object>) row[2] : new HashMap<>())
                            .source((String) row[3])
                            .sourceType((String) row[4])
                            .similarity(((Number) row[5]).floatValue())
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("❌ Error retrieving documents: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Build context from retrieved documents
     */
    private String buildContext(List<RetrievedDocument> documents) {
        if (documents.isEmpty()) {
            return "Không tìm thấy thông tin liên quan trong cơ sở dữ liệu.";
        }

        StringBuilder context = new StringBuilder();
        context.append("Thông tin liên quan từ cơ sở dữ liệu Nike Store:\n\n");

        for (int i = 0; i < documents.size(); i++) {
            RetrievedDocument doc = documents.get(i);
            context.append(String.format("[Nguồn %d - %s - Độ tương đồng: %.2f]\n",
                    i + 1, doc.getSourceType(), doc.getSimilarity()));
            context.append(doc.getContent());
            context.append("\n\n");
        }

        return context.toString();
    }

    /**
     * Get conversation history
     */
    private String getConversationHistory(Long conversationId, int maxMessages) {
        List<ChatMessage> messages = chatMessageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversationId);

        if (messages.isEmpty()) {
            return "";
        }

        // Get last N messages
        int startIndex = Math.max(0, messages.size() - maxMessages);
        List<ChatMessage> recentMessages = messages.subList(startIndex, messages.size());

        StringBuilder history = new StringBuilder();
        history.append("Lịch sử hội thoại gần đây:\n");

        for (ChatMessage msg : recentMessages) {
            history.append(String.format("%s: %s\n",
                    msg.getRole().equals("user") ? "Khách hàng" : "Trợ lý",
                    msg.getContent()));
        }

        return history.toString();
    }

    /**
     * Build prompt for LLM
     */
    private String buildPrompt(String userMessage, String context, String conversationHistory) {
        return String.format(
                """
                        Bạn là trợ lý AI chuyên biệt của Nike Store Vietnam.
                        NHIỆM VỤ: Tư vấn về sản phẩm giày Nike dựa trên dữ liệu được cung cấp.

                        NGUYÊN TẮC QUAN TRỌNG:
                        1. SỬ DỤNG TẤT CẢ thông tin có trong [CONTEXT] để trả lời đầy đủ và chi tiết.
                        2. Khi khách hỏi về sản phẩm: liệt kê TẤT CẢ các sản phẩm phù hợp từ CONTEXT, bao gồm:
                           - Tên sản phẩm
                           - Giá bán
                           - Tình trạng còn hàng
                           - Mô tả chi tiết
                        3. Nếu có nhiều sản phẩm trong CONTEXT, hãy giới thiệu HẾT, không bỏ sót.
                        4. Chỉ nói "không có thông tin" khi CONTEXT thực sự trống hoặc không liên quan.
                        5. TUYỆT ĐỐI KHÔNG tư vấn y tế. Với câu hỏi về sức khỏe -> Khuyên gặp bác sĩ.
                        6. Trả lời đầy đủ, chi tiết, có cấu trúc rõ ràng.

                        %s

                        [CONTEXT - DỮ LIỆU TỪ HỆ THỐNG]:
                        %s

                        [CÂU HỎI]: %s

                        [TRẢ LỜI CHI TIẾT]:
                        """,
                conversationHistory.isEmpty() ? "" : conversationHistory + "\n",
                context,
                userMessage);
    }

    /**
     * Get or create conversation
     */
    private ChatConversation getOrCreateConversation(String sessionId, Long userId) {
        return chatConversationRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    ChatConversation newConversation = ChatConversation.builder()
                            .sessionId(sessionId)
                            .build();

                    if (userId != null) {
                        // Link to user if provided
                        // newConversation.setUser(userRepository.findById(userId).orElse(null));
                    }

                    return chatConversationRepository.save(newConversation);
                });
    }

    /**
     * Get conversation history for a user
     */
    public List<ChatConversation> getUserConversations(Long userId) {
        return chatConversationRepository.findByUserId(userId);
    }

    /**
     * Get messages for a conversation
     */
    public List<ChatMessage> getConversationMessages(String sessionId) {
        return chatConversationRepository.findBySessionId(sessionId)
                .map(conv -> chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conv.getId()))
                .orElse(Collections.emptyList());
    }
}
