package com.foodorder.backend.chatbot.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO cho request gửi tin nhắn đến chatbot
 */
@Data
public class ChatRequestDTO {

    @NotBlank(message = "Tin nhắn không được để trống")
    @Size(max = 2000, message = "Tin nhắn không được vượt quá 2000 ký tự")
    private String message;
    private String sessionId; // Session ID để duy trì ngữ cảnh cuộc hội thoại
    private Long userId; // ID người dùng (có thể null nếu là khách vãng lai)
    private String userContext; // Thông tin bổ sung về người dùng (vị trí, sở thích...)
}
