package com.foodorder.backend.like.entity;

import com.foodorder.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity lưu trữ thông tin lượt thích của người dùng
 * Hỗ trợ like cho nhiều loại đối tượng: Food, Blog, Movie...
 * Sử dụng Unique Index trên (user_id, target_type, target_id) để tránh like trùng lặp
 */
@Entity
@Table(name = "likes",
       uniqueConstraints = {
           @UniqueConstraint(
               name = "uk_likes_user_target",
               columnNames = {"user_id", "target_type", "target_id"}
           )
       },
       indexes = {
           @Index(name = "idx_likes_target", columnList = "target_type, target_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private TargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

