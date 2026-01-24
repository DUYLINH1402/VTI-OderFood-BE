package com.foodorder.backend.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO cho request thay đổi trạng thái user từ admin
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body để Admin thay đổi trạng thái người dùng")
public class AdminUpdateUserStatusRequest {

    @Schema(
        description = "Trạng thái hoạt động (true = active, false = locked)",
        example = "true",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Trạng thái không được để trống")
    private Boolean isActive;

    @Schema(description = "Lý do khóa tài khoản (tùy chọn, chỉ khi isActive = false)", example = "Vi phạm quy định của hệ thống")
    private String reason;
}

