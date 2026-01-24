package com.foodorder.backend.chatbot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO cho request gửi tin nhắn đến chatbot
 */
@Data
@Schema(description = "Request body để gửi tin nhắn đến chatbot AI")
public class ChatRequestDTO {

    @Schema(
        description = "Nội dung tin nhắn gửi đến chatbot (tối đa 2000 ký tự)",
        example = "Tôi muốn đặt món phở bò",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Tin nhắn không được để trống")
    @Size(max = 2000, message = "Tin nhắn không được vượt quá 2000 ký tự")
    private String message;

    @Schema(
        description = "Session ID để duy trì ngữ cảnh cuộc hội thoại",
        example = "session_abc123"
    )
    private String sessionId;

    @Schema(
        description = "ID người dùng (để null nếu là khách vãng lai)",
        example = "1"
    )
    private Long userId;

    @Schema(
        description = "Thông tin bổ sung về người dùng (vị trí, sở thích...)",
        example = "Vị trí: Quận 1, Sở thích: Món Việt"
    )
    private String userContext;
}
