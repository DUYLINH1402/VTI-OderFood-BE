package com.foodorder.backend.user.service;

import com.foodorder.backend.user.dto.request.ChangePasswordRequest;
import com.foodorder.backend.user.dto.request.UserUpdateRequest;
import com.foodorder.backend.user.dto.response.UserResponse;
import com.foodorder.backend.user.entity.User;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserResponse updateProfile(User user, UserUpdateRequest request);
    User save(User user);
    String uploadAvatar(Long userId, MultipartFile file);
    void initiatePasswordReset(String email);
    void resetPassword(String token, String newPassword);
    void changePassword(Long userId, ChangePasswordRequest request);
    
    /**
     * Lấy User theo ID với Role được fetch sẵn để tránh lỗi lazy loading
     */
    User findUserWithRoleById(Long userId);

    /**
     * Lấy User theo ID với Role và RewardPoint được fetch sẵn
     * Dùng cho endpoint lấy profile đầy đủ (bao gồm điểm thưởng)
     */
    User findUserWithRoleAndRewardPointById(Long userId);

    /**
     * Tìm User theo ID
     */
    User findById(Long id);
    
    /**
     * Tìm User theo username với Role được fetch sẵn
     */
    User findByUsername(String username);
}