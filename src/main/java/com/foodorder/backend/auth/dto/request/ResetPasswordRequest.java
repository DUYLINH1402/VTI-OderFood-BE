package com.foodorder.backend.auth.dto.request;

import com.foodorder.backend.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

    @NotBlank(message = "TOKEN_REQUIRED")
    private String token;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @ValidPassword
    private String newPassword;
}
