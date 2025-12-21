package com.proj.webprojrct.review.controller;

import com.proj.webprojrct.review.dto.*;
import com.proj.webprojrct.review.service.ReviewService;
import com.proj.webprojrct.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
@Tag(name = "Review Management", description = "APIs for managing product reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a new review", description = "Add review for a purchased product")
    @ApiResponse(responseCode = "201", description = "Review created successfully")
    @PostMapping
    public ResponseEntity<ReviewDTO> createReview(
            @AuthenticationPrincipal User user,
            @RequestBody CreateReviewRequest request) {
        return ResponseEntity.ok(reviewService.createReview(request, user));
    }

    @Operation(summary = "Get product reviews", description = "Retrieve all reviews for a specific product")
    @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully")
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewDTO>> getProductReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId));
    }

    @Operation(summary = "Filter reviews", description = "Filter reviews with various criteria")
    @ApiResponse(responseCode = "200", description = "Filtered reviews retrieved successfully")
    @PostMapping("/filter")
    public ResponseEntity<org.springframework.data.domain.Page<ReviewDTO>> filterReviews(
            @RequestBody ReviewFilterRequest request) {
        return ResponseEntity.ok(reviewService.filterReviews(request));
    }

    @Operation(summary = "Get review summary", description = "Get review statistics for a product")
    @ApiResponse(responseCode = "200", description = "Review summary retrieved successfully")
    @GetMapping("/product/{productId}/summary")
    public ResponseEntity<ReviewSummaryDTO> getReviewSummary(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getReviewSummary(productId));
    }

    @Operation(summary = "Search reviews", description = "Search reviews by keyword")
    @ApiResponse(responseCode = "200", description = "Search results retrieved successfully")
    @GetMapping("/product/{productId}/search")
    public ResponseEntity<List<ReviewDTO>> searchReviews(
            @PathVariable Long productId,
            @RequestParam String keyword) {
        return ResponseEntity.ok(reviewService.searchReviews(productId, keyword));
    }

    @Operation(summary = "Get sorted reviews", description = "Get reviews sorted by criteria")
    @ApiResponse(responseCode = "200", description = "Sorted reviews retrieved successfully")
    @GetMapping("/product/{productId}/sorted")
    public ResponseEntity<org.springframework.data.domain.Page<ReviewDTO>> getSortedReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "newest") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getSortedReviews(productId, sortBy, page, size));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update review", description = "Update user's existing review")
    @ApiResponse(responseCode = "200", description = "Review updated successfully")
    @PutMapping("/{reviewId}")
    public ResponseEntity<ReviewDTO> updateReview(
            @AuthenticationPrincipal User user,
            @PathVariable Long reviewId,
            @RequestBody UpdateReviewRequest request) {
        return ResponseEntity.ok(reviewService.updateReview(reviewId, request, user));
    }

    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create review reply", description = "Add a reply to a review")
    @ApiResponse(responseCode = "201", description = "Reply created successfully")
    @PostMapping("/{reviewId}/replies")
    public ResponseEntity<ReviewReplyDTO> createReply(
            @AuthenticationPrincipal User user,
            @PathVariable Long reviewId,
            @RequestBody CreateReplyRequest request) {
        return ResponseEntity.ok(reviewService.createReply(reviewId, request, user));
    }

    @Operation(summary = "Get review replies", description = "Get all replies for a review")
    @ApiResponse(responseCode = "200", description = "Replies retrieved successfully")
    @GetMapping("/{reviewId}/replies")
    public ResponseEntity<List<ReviewReplyDTO>> getReviewReplies(@PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewService.getReviewReplies(reviewId));
    }
}