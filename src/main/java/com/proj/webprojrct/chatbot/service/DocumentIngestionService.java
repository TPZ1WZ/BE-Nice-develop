package com.proj.webprojrct.chatbot.service;

import com.pgvector.PGvector;
import com.proj.webprojrct.chatbot.entity.VectorDocument;
import com.proj.webprojrct.chatbot.repository.VectorDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**
 * Service for ingesting documents into vector store
 * Supports PDF processing and chunking
 * Enabled - pgvector extension is now configured
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentIngestionService {

    private final EmbeddingService embeddingService;
    private final VectorDocumentRepository vectorDocumentRepository;

    private static final int CHUNK_SIZE = 500; // characters per chunk
    private static final int CHUNK_OVERLAP = 100; // overlap between chunks

    /**
     * Ingest PDF file
     */
    @Transactional
    public int ingestPDF(MultipartFile file, String sourceType, Map<String, Object> metadata) {
        log.info("📄 Ingesting PDF file: {}", file.getOriginalFilename());

        try {
            // Extract text from PDF
            String text = extractTextFromPDF(file);

            // Split into chunks
            List<String> chunks = chunkText(text);

            log.info("📝 Split into {} chunks", chunks.size());

            // Embed and store each chunk
            int stored = 0;
            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);

                // Generate embedding
                float[] embedding = embeddingService.embed(chunk);

                // Create metadata for chunk
                Map<String, Object> chunkMetadata = new HashMap<>(metadata);
                chunkMetadata.put("chunk_index", i);
                chunkMetadata.put("total_chunks", chunks.size());
                chunkMetadata.put("filename", file.getOriginalFilename());

                // Store in database
                VectorDocument document = VectorDocument.builder()
                        .content(chunk)
                        .metadata(chunkMetadata)
                        .embedding(new PGvector(embedding))
                        .source(file.getOriginalFilename())
                        .sourceType(sourceType != null ? sourceType : "pdf")
                        .build();

                vectorDocumentRepository.save(document);
                stored++;
            }

            log.info("✅ Successfully ingested {} chunks from PDF", stored);
            return stored;

        } catch (IOException e) {
            log.error("❌ Error ingesting PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to ingest PDF", e);
        }
    }

    /**
     * Extract text from PDF
     */
    private String extractTextFromPDF(MultipartFile file) throws IOException {
        try (PDDocument document = org.apache.pdfbox.Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * Ingest plain text
     */
    @Transactional
    public int ingestText(String text, String source, String sourceType, Map<String, Object> metadata) {
        log.info("📝 Ingesting text from source: {}", source);

        try {
            // Split into chunks
            List<String> chunks = chunkText(text);

            log.info("📝 Split into {} chunks", chunks.size());

            // Embed and store each chunk
            int stored = 0;
            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);

                // Generate embedding
                float[] embedding = embeddingService.embed(chunk);

                // Không dùng metadata để tránh lỗi type conversion với JSONB
                // Store in database
                VectorDocument document = VectorDocument.builder()
                        .content(chunk)
                        .metadata(null)  // Set null để tránh lỗi JSONB serialization
                        .embedding(new PGvector(embedding))
                        .source(source)
                        .sourceType(sourceType != null ? sourceType : "text")
                        .build();

                vectorDocumentRepository.save(document);
                stored++;
            }

            log.info("✅ Successfully ingested {} chunks", stored);
            return stored;

        } catch (Exception e) {
            log.error("❌ Error ingesting text: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to ingest text", e);
        }
    }

    /**
     * Chunk text into smaller pieces - SIMPLIFIED to avoid OutOfMemoryError
     */
    private List<String> chunkText(String text) {
        List<String> chunks = new ArrayList<>();

        // For product descriptions (short text), just use as-is without chunking
        if (text.length() <= 1000) {
            chunks.add(text.trim());
            return chunks;
        }

        // For longer text, simple chunking
        int chunkSize = 800;
        for (int i = 0; i < text.length(); i += chunkSize) {
            int end = Math.min(i + chunkSize, text.length());
            chunks.add(text.substring(i, end).trim());
        }

        return chunks;
    }

    /**
     * Delete documents by source
     */
    @Transactional
    public void deleteBySource(String source) {
        log.info("🗑️ Deleting documents from source: {}", source);
        vectorDocumentRepository.deleteBySource(source);
    }

    /**
     * Delete documents by source type
     */
    @Transactional
    public void deleteBySourceType(String sourceType) {
        log.info("🗑️ Deleting documents of type: {}", sourceType);
        vectorDocumentRepository.deleteBySourceType(sourceType);
    }

    /**
     * Get document count by source type
     */
    public long getDocumentCount(String sourceType) {
        if (sourceType != null) {
            return vectorDocumentRepository.countBySourceType(sourceType);
        }
        return vectorDocumentRepository.count();
    }
}
