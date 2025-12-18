package com.foodorder.backend.chat.entity;

import com.foodorder.backend.user.entity.User;
import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity đại diện cho cuộc trò chuyện duy nhất giữa User và Staff
 * Mỗi User chỉ có 1 conversation duy nhất với Staff
 */
@Entity(name = "UserStaffConversation")
@Table(name = "user_staff_conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User sở hữu cuộc trò chuyện này (UNIQUE - mỗi user chỉ có 1 conversation)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /**
     * Thời gian tạo cuộc trò chuyện
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Thời gian cập nhật cuối cùng
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Thời gian tin nhắn cuối cùng
     */
    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    /**
     * Trạng thái hoạt động của cuộc trò chuyện
     */
    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    /**
     * Ghi chú của staff về cuộc trò chuyện này
     */
    @Column(name = "staff_notes", columnDefinition = "TEXT")
    private String staffNotes;

    /**
     * Danh sách tin nhắn trong cuộc trò chuyện
     */
    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChatMessage> messages;

    /**
     * Tạo conversation mới cho user
     */
    public static Conversation createForUser(User user) {
        LocalDateTime now = LocalDateTime.now();
        return Conversation.builder()
                .user(user)
                .createdAt(now)
                .updatedAt(now)
                .isActive(true)
                .build();
    }

    /**
     * Cập nhật thời gian tin nhắn cuối cùng
     */
    public void updateLastMessageTime() {
        this.lastMessageAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Thêm ghi chú từ staff
     */
    public void addStaffNotes(String notes) {
        if (this.staffNotes == null || this.staffNotes.isEmpty()) {
            this.staffNotes = notes;
        } else {
            this.staffNotes += "\n" + LocalDateTime.now() + ": " + notes;
        }
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Vô hiệu hóa cuộc trò chuyện
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Kích hoạt lại cuộc trò chuyện
     */
    public void activate() {
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
