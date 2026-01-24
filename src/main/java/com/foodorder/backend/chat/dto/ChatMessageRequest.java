package com.foodorder.backend.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO cho yêu cầu gửi tin nhắn chat
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body để gửi tin nhắn chat")
public class ChatMessageRequest {

    @Schema(
        description = "Nội dung tin nhắn (tối đa 1000 ký tự)",
        example = "Xin chào, tôi cần hỗ trợ",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    @Size(max = 1000, message = "Tin nhắn không được vượt quá 1000 ký tự")
    private String message;

    @Schema(
        description = "JWT Token xác thực người dùng",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Token xác thực không được để trống")
    private String token;

    /**
     * ID của tin nhắn mà staff đang phản hồi (chỉ dành cho staff reply)
     */
    @Schema(
        description = "ID của tin nhắn gốc mà staff muốn phản hồi (chỉ dành cho staff)",
        example = "msg_123456"
    )
    private String replyToMessageId;

    /**
     * ID của user mà staff muốn gửi tin nhắn riêng (chỉ dành cho staff)
     */
    @Schema(
        description = "ID của user mà staff muốn gửi tin nhắn riêng (chỉ dành cho staff)",
        example = "5"
    )
    private Long recipientUserId;

}
