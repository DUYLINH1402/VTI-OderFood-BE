package com.foodorder.backend.user.dto.response;

import com.foodorder.backend.user.entity.User;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String fullName;
    private String username;
    private String email;
    private String phoneNumber;
    private String avatarUrl;
    private String address;
    private String token;
    private String role;
    private boolean isActive;
    private boolean isVerified;
    private int point;
    private LocalDateTime lastLogin;
    private LocalDateTime updatedAt;

    // Method tiện dụng
    public static UserResponse fromEntity(User user) {
        Integer balance = (user.getRewardPoint() != null) ? user.getRewardPoint().getBalance() : 0;
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .avatarUrl(user.getAvatarUrl())
                .address(user.getAddress())
                .role(user.getRole())
                .isActive(user.isActive())
                .isVerified(user.isVerified())
                .point(balance)
                .lastLogin(user.getLastLogin())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
