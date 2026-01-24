package com.foodorder.backend.auth.service;

import com.foodorder.backend.auth.dto.request.GoogleLoginRequest;
import com.foodorder.backend.auth.dto.request.UserLoginRequest;
import com.foodorder.backend.auth.dto.request.UserRegisterRequest;
import com.foodorder.backend.user.dto.response.UserResponse;

public interface AuthService {
    UserResponse registerUser(UserRegisterRequest request);
    UserResponse loginUser(UserLoginRequest request);
    void resendVerificationEmail(String email);

    /**
     * Đăng nhập bằng Google OAuth 2.0
     * - Verify ID Token với Google
     * - Tạo mới hoặc cập nhật user
     * - Trả về JWT token nội bộ
     * @param request chứa ID Token từ Google
     * @return UserResponse kèm JWT token
     */
    UserResponse loginWithGoogle(GoogleLoginRequest request);
}
