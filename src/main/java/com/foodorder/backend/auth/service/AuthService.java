package com.foodorder.backend.auth.service;

import com.foodorder.backend.auth.dto.request.UserLoginRequest;
import com.foodorder.backend.auth.dto.request.UserRegisterRequest;
import com.foodorder.backend.user.dto.response.UserResponse;

public interface AuthService {
    UserResponse registerUser(UserRegisterRequest request);
    UserResponse loginUser(UserLoginRequest request);
    void resendVerificationEmail(String email);

}
