package com.foodorder.backend.auth.dto.request;

import com.foodorder.backend.validation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request body để đăng nhập tài khoản")
public class UserLoginRequest {

    @Schema(
        description = "Tên đăng nhập hoặc email của người dùng",
        example = "user@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "LOGIN_REQUIRED")
    private String login;

    @Schema(
        description = "Mật khẩu đăng nhập (tối thiểu 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt)",
        example = "Password@123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "PASSWORD_REQUIRED")
    @ValidPassword
    private String password;

}
