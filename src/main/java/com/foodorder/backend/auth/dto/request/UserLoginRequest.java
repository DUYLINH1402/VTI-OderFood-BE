package com.foodorder.backend.auth.dto.request;

import com.foodorder.backend.validation.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequest {

    @NotBlank(message = "LOGIN_REQUIRED")
    private String login;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @ValidPassword
    private String password;

}
