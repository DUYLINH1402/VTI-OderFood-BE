package com.foodorder.backend.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.foodorder.backend.chat.entity.ChatMessage;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO cho phản hồi tin nhắn chat
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResponse {

    private String messageId;
    private String content;
    private Long senderId;
    private String senderName;
    private String senderEmail;
    private Long receiverId;
    private String receiverName;
    private String messageType;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sentAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readAt;

    // ========== REPLY REFERENCE FIELDS ==========
    /**
     * ID của tin nhắn gốc mà tin nhắn này đang phản hồi
     */
    private String replyToMessageId;

    /**
     * Nội dung tin nhắn gốc
     */
    private String replyToText;

    /**
     * Tên người gửi tin nhắn gốc
     */
    private String replyToSenderName;

    /**
     * Thời gian gửi tin nhắn gốc
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime replyToTimestamp;

    /**
     * Chuyển đổi từ Entity sang DTO (không bao gồm reply reference)
     */
    public static ChatMessageResponse fromEntity(ChatMessage chatMessage) {
        ChatMessageResponseBuilder builder = ChatMessageResponse.builder()
                .messageId(chatMessage.getMessageId())
                .content(chatMessage.getContent())
                .senderId(chatMessage.getSender().getId())
                .senderName(chatMessage.getSender().getFullName())
                .senderEmail(chatMessage.getSender().getEmail())
                .messageType(chatMessage.getMessageType().name())
                .status(chatMessage.getStatus().name())
                .sentAt(chatMessage.getSentAt())
                .readAt(chatMessage.getReadAt())
                .replyToMessageId(chatMessage.getReplyToMessageId());

        if (chatMessage.getReceiver() != null) {
            builder.receiverId(chatMessage.getReceiver().getId())
                   .receiverName(chatMessage.getReceiver().getFullName());
        }

        return builder.build();
    }

    /**
     * Chuyển đổi từ Entity sang DTO với đầy đủ thông tin reply reference
     */
    public static ChatMessageResponse fromEntityWithReplyReference(ChatMessage chatMessage, ChatMessage originalMessage) {
        ChatMessageResponse response = fromEntity(chatMessage);
        
        if (originalMessage != null) {
            response.setReplyToText(originalMessage.getContent());
            response.setReplyToSenderName(originalMessage.getSender().getFullName());
            response.setReplyToTimestamp(originalMessage.getSentAt());
        }
        
        return response;
    }
}