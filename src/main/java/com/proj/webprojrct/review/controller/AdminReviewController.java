package com.proj.webprojrct.review.controller;

import com.proj.webprojrct.review.dto.ReviewDTO;
import com.proj.webprojrct.review.entity.Review;
import com.proj.webprojrct.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.proj.webprojrct.review.dto.ReviewFilterRequest;
import com.proj.webprojrct.review.dto.CreateReplyRequest;
import com.proj.webprojrct.review.dto.ReviewReplyDTO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.proj.webprojrct.user.entity.User;
// We might need UserService to ban user, but for now we'll assume we can update User entity directly via repository in Service 
// or assume a AdminUserController exists. The requirement is to add "Lock account" functionality safely.
// Since I don't have UserService injected effectively in ReviewService (I have UserRepository access though User entity relationships), 
// and I cannot easily mod UserService without seeing it, I'll do a simple workaround:
// I'll add the ban logic in ReviewService if not already there? 
// Wait, I updated ReviewService but I didn't add `banUser`. I missed that step in my detailed thought process for ReviewService update!
// I need that `banUser` method in ReviewService or I need to inject UserService here.
// I will check if I can just inject UserRepository here and do it.
import com.proj.webprojrct.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;

@RestController
@RequestMapping("/api/v1/admin/reviews")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Admin Review Management", description = "APIs for admin to manage product reviews")
@PreAuthorize("hasRole('ADMIN') or hasRole('ROOT')")
public class AdminReviewController {

    private final ReviewService reviewService;
    private final UserRepository userRepository; // Direct access for ban function simplicity

    @Operation(summary = "Get all reviews (GET)", description = "Get reviews with query params for admin")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Long productId) {

        return ResponseEntity.ok(reviewService.getAllReviewsForAdmin(page, size, status, rating, productId));
    }

    @Operation(summary = "Get review statistics", description = "Get counts of total, pending, approved reviews")
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getReviewStatistics() {
        return ResponseEntity.ok(reviewService.getReviewStatistics());
    }

    @Operation(summary = "Approve review", description = "Admin: Approve a pending review")
    @PatchMapping("/{reviewId}/approve")
    public ResponseEntity<ReviewDTO> approveReview(@PathVariable Long reviewId) {
        return ResponseEntity.ok(reviewService.approveReview(reviewId));
    }

    @Operation(summary = "Reject/Hide review", description = "Hide a review from public with reason")
    @PutMapping("/{id}/reject")
    public ResponseEntity<Map<String, Object>> rejectReview(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = (body != null) ? body.get("reason") : "Rejected by admin";
        reviewService.rejectReview(id, reason);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Review rejected"));
    }

    @Operation(summary = "Delete review", description = "Admin: Delete a review permanently")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Map<String, Object>> deleteReview(@PathVariable Long reviewId) {
        reviewService.deleteReviewByAdmin(reviewId);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Review deleted successfully"));
    }

    @Operation(summary = "Reply to review", description = "Admin reply")
    @PostMapping("/{id}/admin-reply")
    public ResponseEntity<Map<String, Object>> replyToReview(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        reviewService.addAdminReply(id, body.get("content"));
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Reply added"));
    }

    @Operation(summary = "Get pending reviews", description = "Admin: Get all pending reviews awaiting approval")
    @GetMapping("/pending")
    public ResponseEntity<List<ReviewDTO>> getPendingReviews() {
        var reviews = reviewService.getPendingReviews();
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Bulk approve reviews", description = "Admin: Approve multiple reviews at once")
    @PostMapping("/bulk-approve")
    public ResponseEntity<Map<String, Object>> bulkApproveReviews(@RequestBody Map<String, List<Long>> body) {
        List<Long> reviewIds = body.get("reviewIds");
        int count = reviewService.bulkApproveReviews(reviewIds);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", count + " reviews approved successfully",
                "count", count));
    }

    @Operation(summary = "Bulk delete reviews", description = "Admin: Delete multiple reviews at once")
    @DeleteMapping("/bulk-delete")
    public ResponseEntity<Map<String, Object>> bulkDeleteReviews(@RequestBody Map<String, List<Long>> body) {
        List<Long> reviewIds = body.get("reviewIds");
        int count = reviewService.bulkDeleteReviews(reviewIds);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", count + " reviews deleted successfully",
                "count", count));
    }

    // --- NEW MODERATION ENDPOINTS ---

    @Operation(summary = "Ban User", description = "Admin: Ban a user from reviewing and login")
    @PostMapping("/users/{userId}/ban")
    public ResponseEntity<Map<String, Object>> banUser(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> body) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setIsBanned(true);
        user.setBanReason((String) body.getOrDefault("reason", "Violation of terms"));
        // user.setBanUntil() // Optional

        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "User banned successfully"));
    }

    @Operation(summary = "Delete All User Reviews", description = "Admin: Clean up all reviews from a spammer")
    @DeleteMapping("/users/{userId}/reviews")
    public ResponseEntity<Map<String, Object>> deleteUserReviews(@PathVariable Long userId) {
        reviewService.deleteAllReviewsByUser(userId);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "All reviews from user deleted"));
    }

    @Operation(summary = "Restore Review", description = "Admin: Restore a blocked/hidden review back to SAFE status")
    @PatchMapping("/{reviewId}/restore")
    public ResponseEntity<Map<String, Object>> restoreReview(@PathVariable Long reviewId) {
        Review restored = reviewService.restoreReview(reviewId);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Review restored successfully",
                "review", restored));
    }
}
