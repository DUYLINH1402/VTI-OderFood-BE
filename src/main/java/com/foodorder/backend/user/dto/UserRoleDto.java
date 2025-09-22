package com.foodorder.backend.user.dto;

import com.foodorder.backend.user.entity.User;
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
public class UserRoleDto {
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String roleCode; // Trả về code của role (ROLE_ADMIN, ROLE_STAFF...)
    private String roleName; // Trả về tên hiển thị của role (Quản trị viên, Nhân viên...)
    private boolean isActive;
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
