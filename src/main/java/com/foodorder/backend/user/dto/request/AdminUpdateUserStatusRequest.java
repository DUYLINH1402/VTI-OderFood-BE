package com.foodorder.backend.user.dto.request;

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
public class AdminUpdateUserStatusRequest {

    @NotNull(message = "Trạng thái không được để trống")
    private Boolean isActive;

    // Lý do khóa tài khoản (tùy chọn)
    private String reason;
}

