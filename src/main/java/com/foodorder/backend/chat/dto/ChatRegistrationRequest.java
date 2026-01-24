package com.foodorder.backend.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;


/**
 * DTO cho yêu cầu đăng ký chat
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body để đăng ký kết nối WebSocket chat")
public class ChatRegistrationRequest {

    @Schema(
        description = "JWT Token xác thực người dùng",
        example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Token xác thực không được để trống")
    private String token;
}
