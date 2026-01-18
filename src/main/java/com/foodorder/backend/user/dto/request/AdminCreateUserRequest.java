package com.foodorder.backend.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO cho request tạo user mới từ admin
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCreateUserRequest {

    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 50, message = "Username phải từ 3-50 ký tự")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Size(min = 6, max = 100, message = "Mật khẩu phải từ 6-100 ký tự")
    private String password;

    @Size(max = 100, message = "Họ tên không được quá 100 ký tự")
    private String fullName;

    @Size(max = 20, message = "Số điện thoại không được quá 20 ký tự")
    private String phoneNumber;

    @Size(max = 255, message = "Địa chỉ không được quá 255 ký tự")
    private String address;

    private String avatarUrl;

    // Role code: ROLE_USER, ROLE_STAFF, ROLE_ADMIN
    private String roleCode;

    // Trạng thái active
    private Boolean isActive;

    // Trạng thái verified
    private Boolean isVerified;
}

