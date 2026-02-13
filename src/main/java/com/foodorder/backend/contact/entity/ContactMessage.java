package com.foodorder.backend.contact.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity lưu trữ tin nhắn liên hệ từ khách hàng
 * Dữ liệu được lưu vào DB ngay khi nhận để đảm bảo không mất tin nhắn
 */
@Entity
@Table(name = "contact_messages", indexes = {
    @Index(name = "idx_contact_email", columnList = "email"),
    @Index(name = "idx_contact_status", columnList = "status"),
    @Index(name = "idx_contact_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Tên người gửi
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * Email người gửi
     */
    @Column(nullable = false, length = 255)
    private String email;

    /**
     * Số điện thoại (tùy chọn)
     */
    @Column(length = 20)
    private String phone;

    /**
     * Chủ đề tin nhắn (tùy chọn)
     */
    @Column(length = 200)
    private String subject;

    /**
     * Nội dung tin nhắn
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    /**
     * Trạng thái tin nhắn: PENDING, READ, REPLIED, ARCHIVED
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ContactStatus status = ContactStatus.PENDING;

    /**
     * IP address của người gửi (dùng cho rate limiting và chống spam)
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * Ghi chú nội bộ của admin
     */
    @Column(name = "admin_note", columnDefinition = "TEXT")
    private String adminNote;

    /**
     * Nội dung phản hồi từ admin
     */
    @Column(name = "reply_content", columnDefinition = "TEXT")
    private String replyContent;

    /**
     * Thời gian phản hồi
     */
    @Column(name = "replied_at")
    private LocalDateTime repliedAt;

    /**
     * ID của admin đã phản hồi
     */
    @Column(name = "replied_by")
    private Long repliedBy;

    /**
     * Trạng thái gửi thông báo webhook: true = đã gửi thành công
     */
    @Column(name = "notification_sent")
    @Builder.Default
    private Boolean notificationSent = false;

    /**
     * Thời gian tạo
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Thời gian cập nhật
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

