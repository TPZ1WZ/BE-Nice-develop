package com.proj.webprojrct.review.entity;

import com.proj.webprojrct.common.entity.BaseEntity;
import com.proj.webprojrct.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "review_replies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReviewReply extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "review_id")
    private Review review;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private ReviewReply parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<ReviewReply> children = new ArrayList<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 1000)
    private String comment; // Nội dung phản hồi

    @Column(name = "is_admin_reply", nullable = false)
    @Builder.Default
    private Boolean isAdminReply = false; // Phản hồi từ admin

    @Column(name = "is_hidden")
    private Boolean isHidden = false;

    @Column(name = "like_count")
    private Integer likeCount = 0;

    @Column(name = "dislike_count")
    private Integer dislikeCount = 0;
}
