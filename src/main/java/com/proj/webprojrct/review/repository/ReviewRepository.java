package com.proj.webprojrct.review.repository;

import com.proj.webprojrct.product.entity.Product;
import com.proj.webprojrct.review.entity.Review;
import com.proj.webprojrct.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductOrderByCreatedAtDesc(Product product);
    Optional<Review> findByUserAndProduct(User user, Product product);
    boolean existsByUserAndProduct(User user, Product product);
    List<Review> findAllByUserAndProductIn(User user, List<Product> products);
    
    // Các method mới cho ReviewService
    List<Review> findByProductAndApprovedOrderByCreatedAtDesc(Product product, Boolean approved);
    List<Review> findByProductAndApproved(Product product, Boolean approved);
    Page<Review> findByProductAndApproved(Product product, Boolean approved, Pageable pageable);
    Page<Review> findByProductAndRatingAndApproved(Product product, Integer rating, Boolean approved, Pageable pageable);
    List<Review> findByApprovedOrderByCreatedAtDesc(Boolean approved);
    List<Review> findByProductAndApprovedAndCommentContainingIgnoreCase(Product product, Boolean approved, String keyword);
}