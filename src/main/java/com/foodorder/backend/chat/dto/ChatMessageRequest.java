package com.foodorder.backend.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class ChatMessageRequest {

    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    @Size(max = 1000, message = "Tin nhắn không được vượt quá 1000 ký tự")
    private String message;

    @NotBlank(message = "Token xác thực không được để trống")
    private String token;

    /**
     * ID của tin nhắn mà staff đang phản hồi (chỉ dành cho staff reply)
     */
    private String replyToMessageId;

    /**
     * ID của user mà staff muốn gửi tin nhắn riêng (chỉ dành cho staff)
     */
    private Long recipientUserId;

}
