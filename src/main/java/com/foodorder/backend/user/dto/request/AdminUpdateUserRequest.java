package com.foodorder.backend.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO cho request cập nhật user từ admin
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body để Admin cập nhật thông tin người dùng")
public class AdminUpdateUserRequest {

    @Schema(description = "Tên đăng nhập (3-50 ký tự)", example = "johndoe")
    @Size(min = 3, max = 50, message = "Username phải từ 3-50 ký tự")
    private String username;

    @Schema(description = "Email người dùng", example = "user@example.com")
    @Email(message = "Email không hợp lệ")
    private String email;

    @Schema(description = "Mật khẩu mới (để trống nếu không thay đổi)", example = "NewPassword@123")
    @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6-100 ký tự")
    private String password;

    @Schema(description = "Họ tên đầy đủ", example = "Nguyễn Văn A")
    @Size(max = 100, message = "Họ tên không được quá 100 ký tự")
    private String fullName;

    @Schema(description = "Số điện thoại", example = "0901234567")
    @Size(max = 20, message = "Số điện thoại không được quá 20 ký tự")
    private String phoneNumber;

    @Schema(description = "Địa chỉ", example = "123 Nguyễn Huệ, Quận 1, TP.HCM")
    @Size(max = 255, message = "Địa chỉ không được quá 255 ký tự")
    private String address;

    @Schema(description = "URL avatar", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    @Schema(description = "Mã vai trò", example = "ROLE_USER", allowableValues = {"ROLE_USER", "ROLE_STAFF", "ROLE_ADMIN"})
    private String roleCode;

    @Schema(description = "Trạng thái hoạt động", example = "true")
    private Boolean isActive;

    @Schema(description = "Đã xác thực email chưa", example = "true")
    private Boolean isVerified;
}

