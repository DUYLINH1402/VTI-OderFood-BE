package com.foodorder.backend.chatbot.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity lưu trữ tin nhắn chat giữa người dùng và chatbot
 */
@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private String sessionId;

    @Column(name = "user_id")
    private Long userId; // Có thể null nếu là khách vãng lai

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType; // USER hoặc BOT

    @Column(name = "message_content", columnDefinition = "TEXT", nullable = false)
    private String messageContent;

    @Column(name = "context_used", columnDefinition = "TEXT")
    private String contextUsed; // Context từ RAG được sử dụng để trả lời

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "response_time")
    private Integer responseTime; // Thời gian phản hồi (ms)

    @Column(name = "user_rating")
    private Integer userRating; // Đánh giá từ 1-5 của người dùng

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public enum MessageType {
        USER, BOT
    }
}
