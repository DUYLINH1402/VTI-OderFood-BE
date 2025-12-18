package com.foodorder.backend.chat.entity;

import com.foodorder.backend.user.entity.User;
import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity đại diện cho tin nhắn chat giữa User và Staff
 * LƯU Ý: Sử dụng bảng user_staff_chat_messages để tránh conflict với bảng chat_messages của Chatbot AI
 */
@Entity(name = "UserStaffChatMessage")
@Table(name = "user_staff_chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID của tin nhắn (UUID cho WebSocket)
     */
    @Column(name = "message_id", unique = true, nullable = false)
    private String messageId;

    /**
     * Cuộc trò chuyện mà tin nhắn này thuộc về
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    /**
     * Nội dung tin nhắn
     */
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * Người gửi tin nhắn
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /**
     * Người nhận tin nhắn (có thể null nếu gửi cho tất cả staff)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id")
    private User receiver;

    /**
     * Loại tin nhắn: USER_TO_STAFF, STAFF_TO_USER
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    /**
     * Thời gian gửi tin nhắn
     */
    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    /**
     * Thời gian đọc tin nhắn
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    /**
     * Trạng thái tin nhắn
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private MessageStatus status = MessageStatus.SENT;

    /**
     * Session ID của WebSocket (để tracking)
     */
    @Column(name = "session_id")
    private String sessionId;

    /**
     * ID của tin nhắn gốc mà tin nhắn này đang phản hồi (chỉ áp dụng cho STAFF_TO_USER)
     */
    @Column(name = "reply_to_message_id")
    private String replyToMessageId;

    // ========== SOFT DELETE FIELDS ==========

    /**
     * User đã xóa tin nhắn này (soft delete) - user không thấy tin nhắn này nữa
     */
    @Column(name = "is_deleted_by_user", nullable = false)
    @Builder.Default
    private Boolean isDeletedByUser = false;

    /**
     * Staff đã xóa tin nhắn này (soft delete) - staff không thấy tin nhắn này nữa
     */
    @Column(name = "is_deleted_by_staff", nullable = false)
    @Builder.Default
    private Boolean isDeletedByStaff = false;

    /**
     * Thời gian user xóa tin nhắn
     */
    @Column(name = "deleted_by_user_at")
    private LocalDateTime deletedByUserAt;

    /**
     * Thời gian staff xóa tin nhắn
     */
    @Column(name = "deleted_by_staff_at")
    private LocalDateTime deletedByStaffAt;

    /**
     * Thời gian tạo
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Thời gian cập nhật cuối cùng
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Enum định nghĩa loại tin nhắn
     */
    public enum MessageType {
        USER_TO_STAFF("Khách hàng gửi cho nhân viên"),
        STAFF_TO_USER("Nhân viên trả lời khách hàng");

        private final String description;

        MessageType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Enum định nghĩa trạng thái tin nhắn
     */
    public enum MessageStatus {
        SENT("Đã gửi"),
        DELIVERED("Đã nhận"),
        READ("Đã đọc");

        private final String description;

        MessageStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * Tạo tin nhắn từ User gửi cho Staff
     */
    public static ChatMessage fromUserToStaff(User sender, String content, String messageId, String sessionId) {
        return ChatMessage.builder()
                .messageId(messageId)
                .content(content)
                .sender(sender)
                .messageType(MessageType.USER_TO_STAFF)
                .sentAt(LocalDateTime.now())
                .status(MessageStatus.SENT)
                .sessionId(sessionId)
                .build();
    }

    /**
     * Tạo tin nhắn từ Staff gửi cho User
     */
    public static ChatMessage fromStaffToUser(User sender, User receiver, String content, String messageId) {
        return ChatMessage.builder()
                .messageId(messageId)
                .content(content)
                .sender(sender)
                .receiver(receiver)
                .messageType(MessageType.STAFF_TO_USER)
                .sentAt(LocalDateTime.now())
                .status(MessageStatus.SENT)
                .build();
    }

    /**
     * Tạo tin nhắn từ User gửi cho Staff với conversation
     */
    public static ChatMessage fromUserToStaff(Conversation conversation, User sender, String content, String messageId, String sessionId) {
        LocalDateTime now = LocalDateTime.now();
        return ChatMessage.builder()
                .messageId(messageId)
                .conversation(conversation)
                .content(content)
                .sender(sender)
                .messageType(MessageType.USER_TO_STAFF)
                .sentAt(now)
                .status(MessageStatus.SENT)
                .sessionId(sessionId)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Tạo tin nhắn từ Staff gửi cho User với conversation
     */
    public static ChatMessage fromStaffToUser(Conversation conversation, User sender, User receiver, String content, String messageId) {
        LocalDateTime now = LocalDateTime.now();
        return ChatMessage.builder()
                .messageId(messageId)
                .conversation(conversation)
                .content(content)
                .sender(sender)
                .receiver(receiver)
                .messageType(MessageType.STAFF_TO_USER)
                .sentAt(now)
                .status(MessageStatus.SENT)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Tạo tin nhắn reply từ Staff gửi cho User với conversation và replyToMessageId
     */
    public static ChatMessage fromStaffReplyToUser(Conversation conversation, User sender, User receiver, String content, String messageId, String replyToMessageId) {
        LocalDateTime now = LocalDateTime.now();
        return ChatMessage.builder()
                .messageId(messageId)
                .conversation(conversation)
                .content(content)
                .sender(sender)
                .receiver(receiver)
                .messageType(MessageType.STAFF_TO_USER)
                .replyToMessageId(replyToMessageId)
                .sentAt(now)
                .status(MessageStatus.SENT)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Đánh dấu tin nhắn đã được đọc
     */
    public void markAsRead() {
        this.readAt = LocalDateTime.now();
        this.status = MessageStatus.READ;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * User xóa tin nhắn (soft delete) - user không thấy tin nhắn này nữa nhưng staff vẫn thấy
     */
    public void deleteByUser() {
        this.isDeletedByUser = true;
        this.deletedByUserAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Staff xóa tin nhắn (soft delete) - staff không thấy tin nhắn này nữa
     */
    public void deleteByStaff() {
        this.isDeletedByStaff = true;
        this.deletedByStaffAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * User khôi phục tin nhắn đã xóa
     */
    public void restoreByUser() {
        this.isDeletedByUser = false;
        this.deletedByUserAt = null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Staff khôi phục tin nhắn đã xóa
     */
    public void restoreByStaff() {
        this.isDeletedByStaff = false;
        this.deletedByStaffAt = null;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Kiểm tra tin nhắn có bị user xóa không
     */
    public boolean isVisibleToUser() {
        return !isDeletedByUser;
    }

    /**
     * Kiểm tra tin nhắn có bị staff xóa không
     */
    public boolean isVisibleToStaff() {
        return !isDeletedByStaff;
    }

    /**
     * Kiểm tra tin nhắn có bị xóa hoàn toàn không (cả user và staff đều xóa)
     */
    public boolean isCompletelyDeleted() {
        return isDeletedByUser && isDeletedByStaff;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
        if (sentAt == null) sentAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
