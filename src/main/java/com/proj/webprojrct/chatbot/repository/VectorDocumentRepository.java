package com.proj.webprojrct.chatbot.repository;

import com.proj.webprojrct.chatbot.entity.VectorDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository for VectorDocument entity with pgvector support
 */
@Repository
public interface VectorDocumentRepository extends JpaRepository<VectorDocument, Long> {
    
    /**
     * Search for similar documents using cosine similarity
     */
    @Query(value = "SELECT id, content, metadata, source, source_type, " +
            "1 - (embedding <=> CAST(:queryVector AS vector)) AS similarity " +
            "FROM vector_documents " +
            "WHERE (:sourceType IS NULL OR source_type = :sourceType) " +
            "AND 1 - (embedding <=> CAST(:queryVector AS vector)) >= :threshold " +
            "ORDER BY similarity DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Object[]> searchSimilar(
            @Param("queryVector") String queryVector,
            @Param("threshold") float threshold,
            @Param("limit") int limit,
            @Param("sourceType") String sourceType
    );
    
    /**
     * Find all documents by source
     */
    List<VectorDocument> findBySource(String source);
    
    /**
     * Find all documents by source type
     */
    List<VectorDocument> findBySourceType(String sourceType);
    
    /**
     * Delete all documents by source
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM VectorDocument v WHERE v.source = :source")
    void deleteBySource(@Param("source") String source);
    
    /**
     * Delete all documents by source type
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM VectorDocument v WHERE v.sourceType = :sourceType")
    void deleteBySourceType(@Param("sourceType") String sourceType);
    
    /**
     * Count documents by source type
     */
    long countBySourceType(String sourceType);
}
