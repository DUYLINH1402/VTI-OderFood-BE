package com.foodorder.backend.user.dto.response;

import com.foodorder.backend.user.entity.User;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO response cho admin xem thông tin user
 * Bao gồm đầy đủ thông tin hơn so với UserResponse thông thường
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phoneNumber;
    private String avatarUrl;
    private String address;
    private String roleCode;
    private String roleName;
    private boolean isActive;
    private boolean isVerified;
    private Integer point;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Chuyển đổi từ Entity sang DTO
     */
    public static AdminUserResponse fromEntity(User user) {
        Integer balance = (user.getRewardPoint() != null) ? user.getRewardPoint().getBalance() : 0;
        return AdminUserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .address(user.getAddress())
                .roleCode(user.getRole() != null ? user.getRole().getCode() : null)
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .isActive(user.isActive())
                .isVerified(user.isVerified())
                .point(balance)
                .lastLogin(user.getLastLogin())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
