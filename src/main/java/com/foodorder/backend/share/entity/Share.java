package com.foodorder.backend.share.entity;

import com.foodorder.backend.like.entity.TargetType;
import com.foodorder.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity lưu vết lượt chia sẻ của người dùng
 * Hỗ trợ share cho nhiều loại đối tượng: Food, Blog...
 * user_id có thể null nếu cho phép khách vãng lai share
 */
@Entity
@Table(name = "shares",
       indexes = {
           @Index(name = "idx_shares_target", columnList = "target_type, target_id"),
           @Index(name = "idx_shares_user", columnList = "user_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Share {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private TargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 20)
    private SharePlatform platform;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

