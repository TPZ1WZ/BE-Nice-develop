package com.proj.webprojrct.review.entity;

import com.proj.webprojrct.common.entity.BaseEntity;
import com.proj.webprojrct.product.entity.Product;
import com.proj.webprojrct.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Integer rating; // Đánh giá từ 1-5 sao

    @Column(length = 1000)
    private String comment; // Nội dung bình luận

    @Column(name = "title", length = 255)
    private String title; // Tiêu đề đánh giá (tùy chọn)

    @Column(name = "is_hidden")
    private Boolean isHidden; // Đã bị ẩn

    @Column(name = "approved", nullable = false)
    @Builder.Default
    private Boolean approved = false; // Đã được duyệt bởi admin

    // --- MODERATION FIELDS ---
    @Enumerated(EnumType.STRING)
    @Column(name = "review_status")
    private ReviewStatus reviewStatus; // SAFE, WARNING, BLOCK

    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    @Column(name = "ai_suggestion")
    private String aiSuggestion; // approve | hide | require_review

    @Column(name = "ai_reasons", columnDefinition = "text[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> aiReasons;
    // -------------------------

    @Column(name = "like_count")
    private Integer likeCount; // Số lượt đánh giá hữu ích

    @Column(name = "dislike_count")
    private Integer dislikeCount; // Số lượt đánh giá không hữu ích

    @Column(columnDefinition = "text[]")
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> images;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<ReviewReply> replies = new ArrayList<>();

    // Helper method for service layer
    public boolean isApproved() {
        return approved != null && approved;
    }
}