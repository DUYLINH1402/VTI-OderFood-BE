package com.foodorder.backend.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;


/**
 * DTO cho yêu cầu đăng ký chat
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRegistrationRequest {

    @NotBlank(message = "Token xác thực không được để trống")
    private String token;
}
