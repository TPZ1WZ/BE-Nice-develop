package com.proj.webprojrct.review.service;

import com.proj.webprojrct.review.dto.*;
import com.proj.webprojrct.review.entity.Review;
import com.proj.webprojrct.review.entity.ReviewReply;
import com.proj.webprojrct.review.repository.ReviewRepository;
import com.proj.webprojrct.review.repository.ReviewReplyRepository;
import com.proj.webprojrct.product.repository.ProductRepository;
import com.proj.webprojrct.order.repository.OrderRepository;
import com.proj.webprojrct.user.entity.User;
import com.proj.webprojrct.product.entity.Product;
import com.proj.webprojrct.order.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    /**
     * Tạo đánh giá mới
     */
    public ReviewDTO createReview(CreateReviewRequest request, User user) {
        // Validate product exists
        Product product = productRepository.findById(request.getProductId())
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        // Validate order exists and belongs to user
        if (request.getOrderId() != null) {
            Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));
            
            if (!order.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Order does not belong to this user");
            }
            
            // Check if order is completed
            if (!"COMPLETED".equals(order.getStatus())) {
                throw new RuntimeException("Can only review completed orders");
            }
        }
        
        // Check if user already reviewed this product
        if (reviewRepository.existsByUserAndProduct(user, product)) {
            throw new RuntimeException("You have already reviewed this product");
        }
        
        // Create review
        Review review = Review.builder()
            .user(user)
            .product(product)
            .rating(request.getRating())
            .comment(request.getComment())
            .title(request.getTitle())
            .images(request.getImages())
            .approved(false) // Requires admin approval
            .build();
        
        Review savedReview = reviewRepository.save(review);
        
        return convertToDTO(savedReview);
    }

    /**
     * Lấy tất cả đánh giá của sản phẩm
     */
    public List<ReviewDTO> getProductReviews(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        List<Review> reviews = reviewRepository.findByProductAndApprovedOrderByCreatedAtDesc(product, true);
        
        return reviews.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Lọc đánh giá theo tiêu chí
     */
    public Page<ReviewDTO> filterReviews(ReviewFilterRequest request) {
        Pageable pageable = PageRequest.of(
            request.getPage(), 
            request.getPageSize(),
            getSortByField(request.getSortBy())
        );
        
        Page<Review> reviewPage;
        
        if (request.getProductId() != null) {
            Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));
            
            if (request.getRating() != null) {
                Boolean approved = request.getApproved() != null ? request.getApproved() : true;
                reviewPage = reviewRepository.findByProductAndRatingAndApproved(
                    product, request.getRating(), approved, pageable);
            } else {
                Boolean approved = request.getApproved() != null ? request.getApproved() : true;
                reviewPage = reviewRepository.findByProductAndApproved(product, approved, pageable);
            }
        } else {
            reviewPage = reviewRepository.findAll(pageable);
        }
        
        return reviewPage.map(this::convertToDTO);
    }

    /**
     * Lấy thống kê đánh giá sản phẩm
     */
    public ReviewSummaryDTO getReviewSummary(Long productId) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        List<Review> reviews = reviewRepository.findByProductAndApproved(product, true);
        
        if (reviews.isEmpty()) {
            return ReviewSummaryDTO.builder()
                .productId(productId)
                .averageRating(0.0)
                .totalReviews(0L)
                .fiveStars(0L)
                .fourStars(0L)
                .threeStars(0L)
                .twoStars(0L)
                .oneStar(0L)
                .build();
        }
        
        double averageRating = reviews.stream()
            .mapToInt(Review::getRating)
            .average()
            .orElse(0.0);
        
        long fiveStars = reviews.stream().filter(r -> r.getRating() == 5).count();
        long fourStars = reviews.stream().filter(r -> r.getRating() == 4).count();
        long threeStars = reviews.stream().filter(r -> r.getRating() == 3).count();
        long twoStars = reviews.stream().filter(r -> r.getRating() == 2).count();
        long oneStar = reviews.stream().filter(r -> r.getRating() == 1).count();
        
        return ReviewSummaryDTO.builder()
            .productId(productId)
            .averageRating(Math.round(averageRating * 10.0) / 10.0)
            .totalReviews((long) reviews.size())
            .fiveStars(fiveStars)
            .fourStars(fourStars)
            .threeStars(threeStars)
            .twoStars(twoStars)
            .oneStar(oneStar)
            .build();
    }

    /**
     * Tìm kiếm đánh giá theo từ khóa
     */
    public List<ReviewDTO> searchReviews(Long productId, String keyword) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        List<Review> reviews = reviewRepository.findByProductAndApprovedAndCommentContainingIgnoreCase(
            product, true, keyword);
        
        return reviews.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Lấy đánh giá đã sắp xếp
     */
    public Page<ReviewDTO> getSortedReviews(Long productId, String sortBy, int page, int size) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found"));
        
        Pageable pageable = PageRequest.of(page, size, getSortByField(sortBy));
        
        Page<Review> reviewPage = reviewRepository.findByProductAndApproved(product, true, pageable);
        
        return reviewPage.map(this::convertToDTO);
    }

    /**
     * Cập nhật đánh giá
     */
    public ReviewDTO updateReview(Long reviewId, UpdateReviewRequest request, User user) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Review not found"));
        
        // Check if user owns this review
        if (!review.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only update your own reviews");
        }
        
        // Update fields
        if (request.getRating() != null) {
            review.setRating(request.getRating());
        }
        if (request.getComment() != null) {
            review.setComment(request.getComment());
        }
        if (request.getTitle() != null) {
            review.setTitle(request.getTitle());
        }
        if (request.getImages() != null) {
            review.setImages(request.getImages());
        }
        
        review.setUpdatedAt(LocalDateTime.now());
        review.setApproved(false); // Requires re-approval after update
        
        Review updatedReview = reviewRepository.save(review);
        
        return convertToDTO(updatedReview);
    }

    /**
     * Tạo phản hồi đánh giá
     */
    public ReviewReplyDTO createReply(Long reviewId, CreateReplyRequest request, User user) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Review not found"));
        
        ReviewReply reply = ReviewReply.builder()
            .review(review)
            .user(user)
            .comment(request.getComment())
            .isAdminReply(user.getRole().name().contains("ADMIN") || user.getRole().name().contains("ROOT"))
            .build();
        
        ReviewReply savedReply = reviewReplyRepository.save(reply);
        
        return convertReplyToDTO(savedReply);
    }

    /**
     * Lấy tất cả phản hồi của đánh giá
     */
    public List<ReviewReplyDTO> getReviewReplies(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Review not found"));
        
        List<ReviewReply> replies = reviewReplyRepository.findByReviewOrderByCreatedAtAsc(review);
        
        return replies.stream()
            .map(this::convertReplyToDTO)
            .collect(Collectors.toList());
    }

    // ========== ADMIN METHODS ==========

    /**
     * Duyệt đánh giá (Admin)
     */
    public ReviewDTO approveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Review not found"));
        
        review.setApproved(true);
        review.setUpdatedAt(LocalDateTime.now());
        
        Review updatedReview = reviewRepository.save(review);
        
        return convertToDTO(updatedReview);
    }

    /**
     * Ẩn đánh giá (Admin)
     */
    public ReviewDTO hideReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Review not found"));
        
        review.setApproved(false);
        review.setUpdatedAt(LocalDateTime.now());
        
        Review updatedReview = reviewRepository.save(review);
        
        return convertToDTO(updatedReview);
    }

    /**
     * Lấy đánh giá chờ duyệt (Admin)
     */
    public List<ReviewDTO> getPendingReviews() {
        List<Review> pendingReviews = reviewRepository.findByApprovedOrderByCreatedAtDesc(false);
        
        return pendingReviews.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    /**
     * Xóa đánh giá (Admin)
     */
    public void deleteReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new RuntimeException("Review not found"));
        
        // Delete all replies first
        reviewReplyRepository.deleteByReview(review);
        
        // Delete review
        reviewRepository.delete(review);
    }

    // ========== HELPER METHODS ==========

    private Sort getSortByField(String sortBy) {
        if (sortBy == null) sortBy = "newest";
        
        return switch (sortBy.toLowerCase()) {
            case "oldest" -> Sort.by("createdAt").ascending();
            case "rating_high" -> Sort.by("rating").descending();
            case "rating_low" -> Sort.by("rating").ascending();
            case "newest" -> Sort.by("createdAt").descending();
            default -> Sort.by("createdAt").descending();
        };
    }

    private ReviewDTO convertToDTO(Review review) {
        return ReviewDTO.builder()
            .id(review.getId())
            .productId(review.getProduct().getId())
            .productName(review.getProduct().getName())
            .userId(review.getUser().getId())
            .userName(review.getUser().getFullName())
            .rating(review.getRating())
            .comment(review.getComment())
            .title(review.getTitle())
            .images(review.getImages())
            .createdAt(review.getCreatedAt())
            .approved(review.isApproved())
            .build();
    }

    private ReviewReplyDTO convertReplyToDTO(ReviewReply reply) {
        return ReviewReplyDTO.builder()
            .id(reply.getId())
            .reviewId(reply.getReview().getId())
            .userId(reply.getUser().getId())
            .userName(reply.getUser().getFullName())
            .comment(reply.getComment())
            .createdAt(reply.getCreatedAt())
            .isAdminReply(reply.getIsAdminReply())
            .build();
    }
}