package com.foodorder.backend.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request body để yêu cầu đặt lại mật khẩu")
public class ForgotPasswordRequest {

    @Schema(
        description = "Email của người dùng cần đặt lại mật khẩu",
        example = "user@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

}
