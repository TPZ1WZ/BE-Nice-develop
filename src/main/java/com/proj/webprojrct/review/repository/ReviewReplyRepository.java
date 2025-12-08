package com.proj.webprojrct.review.repository;

import com.proj.webprojrct.review.entity.Review;
import com.proj.webprojrct.review.entity.ReviewReply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewReplyRepository extends JpaRepository<ReviewReply, Long> {
    Page<ReviewReply> findByReviewIdAndParentIsNullOrderByCreatedAtAsc(Long reviewId, Pageable pageable);
    
    // Các method mới cho ReviewService
    List<ReviewReply> findByReviewOrderByCreatedAtAsc(Review review);
    List<ReviewReply> findByReviewOrderByCreatedAtDesc(Review review);
    long countByReview(Review review);
    void deleteByReview(Review review);
    boolean existsByReview(Review review);
}
