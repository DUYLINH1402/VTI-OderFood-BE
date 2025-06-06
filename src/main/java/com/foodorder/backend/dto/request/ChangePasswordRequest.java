package com.foodorder.backend.dto.request;

import com.foodorder.backend.validation.ValidPassword;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @ValidPassword
    private String currentPassword;
    @ValidPassword
    private String newPassword;
}

