package com.foodorder.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLoginRequest {

    @NotBlank(message = "LOGIN_REQUIRED")
    private String login;

    @NotBlank(message = "PASSWORD_REQUIRED")
    private String password;

}
