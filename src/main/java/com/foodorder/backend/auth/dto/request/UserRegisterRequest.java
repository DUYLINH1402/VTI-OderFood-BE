package com.foodorder.backend.auth.dto.request;

import com.foodorder.backend.validation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request body để đăng ký tài khoản mới")
public class UserRegisterRequest {

    @Schema(
        description = "Tên đăng nhập của người dùng (không được trùng với tài khoản khác)",
        example = "johndoe",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "USERNAME_REQUIRED")
    private String username;

    @Schema(
        description = "Email của người dùng (dùng để xác thực và nhận thông báo)",
        example = "user@example.com",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "EMAIL_REQUIRED")
    @Email(message = "EMAIL_INVALID")
    private String email;

    @Schema(
        description = "Mật khẩu (tối thiểu 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt)",
        example = "Password@123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "PASSWORD_REQUIRED")
    @ValidPassword
    private String password;

}
