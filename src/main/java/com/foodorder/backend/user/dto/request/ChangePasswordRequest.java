package com.foodorder.backend.user.dto.request;

import com.foodorder.backend.validation.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request body để đổi mật khẩu")
public class ChangePasswordRequest {

    @Schema(
        description = "Mật khẩu hiện tại",
        example = "OldPassword@123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @ValidPassword
    private String currentPassword;

    @Schema(
        description = "Mật khẩu mới (tối thiểu 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt)",
        example = "NewPassword@123",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @ValidPassword
    private String newPassword;
}

