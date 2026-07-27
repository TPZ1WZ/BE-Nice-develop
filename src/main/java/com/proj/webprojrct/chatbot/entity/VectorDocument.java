package com.proj.webprojrct.chatbot.entity;

import com.pgvector.PGvector;
import com.proj.webprojrct.chatbot.config.PGvectorType;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;

// Enabled - pgvector extension is now configured
@Entity
@Table(name = "vector_documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Type(PGvectorType.class)
    @Column(columnDefinition = "vector(384)")
    private PGvector embedding;

    @Column(length = 255)
    private String source;

    @Column(name = "source_type", length = 50)
    private String sourceType; // 'pdf', 'product', 'faq', 'policy'

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
