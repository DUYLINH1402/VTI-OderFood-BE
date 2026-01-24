package com.foodorder.backend.comment.entity;

import com.foodorder.backend.like.entity.TargetType;
import com.foodorder.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity lưu trữ bình luận dùng chung (Polymorphic)
 * Hỗ trợ bình luận cho nhiều loại đối tượng: Food, Blog, Movie...
 * Hỗ trợ bình luận phân cấp (reply comment) thông qua parent_id
 */
@Entity
@Table(name = "comments",
       indexes = {
           @Index(name = "idx_comments_target", columnList = "target_type, target_id"),
           @Index(name = "idx_comments_user", columnList = "user_id"),
           @Index(name = "idx_comments_parent", columnList = "parent_id"),
           @Index(name = "idx_comments_created_at", columnList = "created_at")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Người dùng thực hiện bình luận
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Nội dung bình luận
     */
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * Loại đối tượng được bình luận (FOOD, BLOG, MOVIE...)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private TargetType targetType;

    /**
     * ID của đối tượng được bình luận (id của món ăn hoặc bài viết)
     */
    @Column(name = "target_id", nullable = false)
    private Long targetId;

    /**
     * Trạng thái bình luận (ACTIVE, HIDDEN, DELETED)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CommentStatus status = CommentStatus.ACTIVE;

    /**
     * Bình luận cha (null nếu là comment gốc, có giá trị nếu là reply)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    /**
     * Danh sách các bình luận con (replies)
     */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> replies = new ArrayList<>();

    /**
     * Thời gian tạo bình luận
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Thời gian cập nhật bình luận
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

