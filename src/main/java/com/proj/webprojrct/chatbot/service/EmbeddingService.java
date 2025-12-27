package com.proj.webprojrct.chatbot.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2.AllMiniLmL6V2EmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for generating embeddings from text
 * Uses all-MiniLM-L6-v2 model (384 dimensions) - runs locally, no API calls needed
 */
@Service
@Slf4j
public class EmbeddingService {

    private EmbeddingModel embeddingModel;
    private boolean isInitialized = false;

    public EmbeddingService() {
        try {
            log.info("🔧 Initializing Embedding Model (all-MiniLM-L6-v2)...");
            this.embeddingModel = new AllMiniLmL6V2EmbeddingModel();
            this.isInitialized = true;
            log.info("✅ Embedding Model initialized successfully");
        } catch (Exception e) {
            log.warn("⚠️ Failed to initialize Embedding Model: {}. RAG features will be disabled.", e.getMessage());
            this.embeddingModel = null;
            this.isInitialized = false;
        }
    }

    /**
     * Generate embedding for a single text
     */
    public float[] embed(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be null or empty");
        }
        
        if (!isInitialized || embeddingModel == null) {
            throw new RuntimeException("Embedding model is not initialized");
        }

        try {
            Embedding embedding = embeddingModel.embed(text).content();
            List<Float> vector = embedding.vectorAsList();
            float[] result = new float[vector.size()];
            for (int i = 0; i < vector.size(); i++) {
                result[i] = vector.get(i);
            }
            return result;
        } catch (Exception e) {
            log.error("❌ Error generating embedding for text: {}", text.substring(0, Math.min(100, text.length())), e);
            throw new RuntimeException("Failed to generate embedding", e);
        }
    }

    /**
     * Generate embeddings for multiple texts
     */
    public List<float[]> embedAll(List<String> texts) {
        return texts.stream()
                .map(this::embed)
                .collect(Collectors.toList());
    }

    /**
     * Convert float array to PostgreSQL vector format
     */
    public String toVectorString(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Get embedding dimension
     */
    public int getDimension() {
        return 384; // all-MiniLM-L6-v2 produces 384-dimensional vectors
    }
}
