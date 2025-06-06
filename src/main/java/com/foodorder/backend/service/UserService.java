package com.foodorder.backend.service;

import com.foodorder.backend.dto.request.ChangePasswordRequest;
import com.foodorder.backend.dto.request.UserUpdateRequest;
import com.foodorder.backend.dto.response.UserResponse;
import com.foodorder.backend.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserResponse updateProfile(User user, UserUpdateRequest request);
    User save(User user);
    String uploadAvatar(Long userId, MultipartFile file);
    void initiatePasswordReset(String email);
    void resetPassword(String token, String newPassword);


    void changePassword(Long userId, ChangePasswordRequest request);

}