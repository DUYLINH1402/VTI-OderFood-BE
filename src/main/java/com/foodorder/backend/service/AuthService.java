package com.foodorder.backend.service;

import com.foodorder.backend.dto.request.UserLoginRequest;
import com.foodorder.backend.dto.request.UserRegisterRequest;
import com.foodorder.backend.dto.request.UserUpdateRequest;
import com.foodorder.backend.dto.response.UserResponse;
import com.foodorder.backend.dto.response.UserResponse;
import com.foodorder.backend.entity.User;

public interface AuthService {
    UserResponse registerUser(UserRegisterRequest request);
    UserResponse loginUser(UserLoginRequest request);

}
