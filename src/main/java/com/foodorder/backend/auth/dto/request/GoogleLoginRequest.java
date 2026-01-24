package com.foodorder.backend.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO để nhận ID Token từ Google OAuth 2.0 từ Frontend
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request đăng nhập bằng Google OAuth 2.0")
public class GoogleLoginRequest {

    @NotBlank(message = "ID Token không được để trống")
    @Schema(description = "ID Token từ Google OAuth",
            example = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6IjEyMzQ1Njc4OTAifQ...")
    private String idToken;
}

