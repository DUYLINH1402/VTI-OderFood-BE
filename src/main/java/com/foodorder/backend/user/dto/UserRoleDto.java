package com.foodorder.backend.user.dto;

import com.foodorder.backend.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO để quản lý thông tin vai trò người dùng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO chứa thông tin vai trò của người dùng")
public class UserRoleDto {

    @Schema(description = "ID của người dùng", example = "1")
    private Long userId;

    @Schema(description = "Tên đăng nhập", example = "johndoe")
    private String username;

    @Schema(description = "Email", example = "user@example.com")
    private String email;

    @Schema(description = "Họ tên đầy đủ", example = "Nguyễn Văn A")
    private String fullName;

    @Schema(description = "Mã vai trò", example = "ROLE_USER", allowableValues = {"ROLE_USER", "ROLE_STAFF", "ROLE_ADMIN"})
    private String roleCode;

    @Schema(description = "Tên vai trò hiển thị", example = "Khách hàng")
    private String roleName;

    @Schema(description = "Trạng thái hoạt động", example = "true")
    private boolean isActive;

    @Schema(description = "Đã xác thực email chưa", example = "true")
    private boolean isVerified;

    /**
     * Tạo DTO từ User entity
     */
    public static UserRoleDto fromUser(User user) {
        return UserRoleDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roleCode(user.getRole() != null ? user.getRole().getCode() : null)
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .isActive(user.isActive())
                .isVerified(user.isVerified())
                .build();
    }
}
