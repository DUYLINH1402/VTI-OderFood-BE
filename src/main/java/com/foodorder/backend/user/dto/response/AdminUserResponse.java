package com.foodorder.backend.user.dto.response;

import com.foodorder.backend.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Response chứa thông tin chi tiết người dùng (dành cho Admin)")
public class AdminUserResponse {

    @Schema(description = "ID của người dùng", example = "1")
    private Long id;

    @Schema(description = "Tên đăng nhập", example = "johndoe")
    private String username;

    @Schema(description = "Email", example = "user@example.com")
    private String email;

    @Schema(description = "Họ tên đầy đủ", example = "Nguyễn Văn A")
    private String fullName;

    @Schema(description = "Số điện thoại", example = "0901234567")
    private String phoneNumber;

    @Schema(description = "URL avatar", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    @Schema(description = "Địa chỉ", example = "123 Nguyễn Huệ, Quận 1, TP.HCM")
    private String address;

    @Schema(description = "Mã vai trò", example = "ROLE_USER")
    private String roleCode;

    @Schema(description = "Tên vai trò hiển thị", example = "Khách hàng")
    private String roleName;

    @Schema(description = "Trạng thái hoạt động", example = "true")
    private boolean isActive;

    @Schema(description = "Đã xác thực email chưa", example = "true")
    private boolean isVerified;

    @Schema(description = "Số điểm thưởng hiện tại", example = "500")
    private Integer point;

    @Schema(description = "Thời gian đăng nhập gần nhất", example = "2025-01-20T10:30:00")
    private LocalDateTime lastLogin;

    @Schema(description = "Thời gian tạo tài khoản", example = "2024-06-15T09:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Thời gian cập nhật gần nhất", example = "2025-01-20T10:30:00")
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
