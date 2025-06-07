package com.foodorder.backend.dto.request;

import com.foodorder.backend.validation.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegisterRequest {

    @NotBlank(message = "USERNAME_REQUIRED")
    private String username;

    @NotBlank(message = "EMAIL_REQUIRED")
    @Email(message = "EMAIL_INVALID")
    private String email;

    @NotBlank(message = "PASSWORD_REQUIRED")
    @ValidPassword
    private String password;

}
