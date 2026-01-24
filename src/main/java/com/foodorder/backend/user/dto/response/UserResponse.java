package com.foodorder.backend.user.dto.response;

import com.foodorder.backend.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response chứa thông tin người dùng")
public class UserResponse {

    @Schema(description = "ID của người dùng", example = "1")
    private Long id;

    @Schema(description = "Họ tên đầy đủ", example = "Nguyễn Văn A")
    private String fullName;

    @Schema(description = "Tên đăng nhập", example = "johndoe")
    private String username;

    @Schema(description = "Email", example = "user@example.com")
    private String email;

    @Schema(description = "Số điện thoại", example = "0901234567")
    private String phoneNumber;

    @Schema(description = "URL avatar", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    @Schema(description = "Địa chỉ", example = "123 Nguyễn Huệ, Quận 1, TP.HCM")
    private String address;

    @Schema(description = "JWT token (chỉ trả về khi đăng nhập)", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;

    @Schema(description = "Mã vai trò", example = "ROLE_USER", allowableValues = {"ROLE_USER", "ROLE_STAFF", "ROLE_ADMIN"})
    private String roleCode;

    @Schema(description = "Tên vai trò hiển thị", example = "Khách hàng")
    private String roleName;

    @Schema(description = "Trạng thái hoạt động", example = "true")
    private boolean isActive;

    @Schema(description = "Đã xác thực email chưa", example = "true")
    private boolean isVerified;

    @Schema(description = "Số điểm thưởng hiện tại", example = "500")
    private int point;

    @Schema(description = "Thời gian đăng nhập gần nhất", example = "2025-01-20T10:30:00")
    private LocalDateTime lastLogin;

    @Schema(description = "Thời gian cập nhật gần nhất", example = "2025-01-20T10:30:00")
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
                .roleCode(user.getRole() != null ? user.getRole().getCode() : null)
                .roleName(user.getRole() != null ? user.getRole().getName() : null)
                .isActive(user.isActive())
                .isVerified(user.isVerified())
                .point(balance)
                .lastLogin(user.getLastLogin())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
