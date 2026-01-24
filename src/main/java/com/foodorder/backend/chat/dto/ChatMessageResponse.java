package com.foodorder.backend.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.foodorder.backend.chat.entity.ChatMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO cho phản hồi tin nhắn chat
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response chứa thông tin tin nhắn chat")
public class ChatMessageResponse {

    @Schema(description = "ID của tin nhắn", example = "msg_123456")
    private String messageId;

    @Schema(description = "Nội dung tin nhắn", example = "Xin chào, tôi cần hỗ trợ")
    private String content;

    @Schema(description = "ID của người gửi", example = "1")
    private Long senderId;

    @Schema(description = "Tên người gửi", example = "Nguyễn Văn A")
    private String senderName;

    @Schema(description = "Email người gửi", example = "user@example.com")
    private String senderEmail;

    @Schema(description = "ID của người nhận", example = "2")
    private Long receiverId;

    @Schema(description = "Tên người nhận", example = "Staff 01")
    private String receiverName;

    @Schema(description = "Loại tin nhắn", example = "USER_TO_STAFF", allowableValues = {"USER_TO_STAFF", "STAFF_TO_USER", "SYSTEM"})
    private String messageType;

    @Schema(description = "Trạng thái tin nhắn", example = "SENT", allowableValues = {"SENT", "DELIVERED", "READ"})
    private String status;

    @Schema(description = "Thời gian gửi tin nhắn", example = "2025-01-20 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sentAt;

    @Schema(description = "Thời gian đọc tin nhắn", example = "2025-01-20 10:31:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime readAt;

    // ========== REPLY REFERENCE FIELDS ==========
    /**
     * ID của tin nhắn gốc mà tin nhắn này đang phản hồi
     */
    @Schema(description = "ID của tin nhắn gốc mà tin nhắn này đang phản hồi", example = "msg_123455")
    private String replyToMessageId;

    /**
     * Nội dung tin nhắn gốc
     */
    @Schema(description = "Nội dung tin nhắn gốc được trích dẫn", example = "Tôi muốn hỏi về đơn hàng...")
    private String replyToText;

    /**
     * Tên người gửi tin nhắn gốc
     */
    @Schema(description = "Tên người gửi tin nhắn gốc", example = "Nguyễn Văn A")
    private String replyToSenderName;

    /**
     * Thời gian gửi tin nhắn gốc
     */
    @Schema(description = "Thời gian gửi tin nhắn gốc", example = "2025-01-20 10:25:00")
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