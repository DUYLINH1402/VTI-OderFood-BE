package com.foodorder.backend.notifications.entity;

import com.foodorder.backend.user.entity.User;
import com.foodorder.backend.order.entity.Order;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho bảng notifications trong database
 * Chứa thông tin về các thông báo gửi đến người dùng và nhân viên
 */
@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "order_code", nullable = false, length = 50)
    private String orderCode;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    // Thêm các trường mới để hỗ trợ thông báo cho Staff
    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type", nullable = false, length = 20)
    @Builder.Default
    private RecipientType recipientType = RecipientType.USER;

    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    // Quan hệ với User (có thể null nếu là thông báo cho staff)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    // Quan hệ với Order
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    private Order order;

    /**
     * Đánh dấu thông báo đã được đọc
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    /**
     * Enum định nghĩa loại người nhận thông báo
     */
    public enum RecipientType {
        USER,    // Thông báo cho khách hàng
        STAFF,   // Thông báo cho nhân viên
        ADMIN    // Thông báo cho admin
    }
}
