package com.foodorder.backend.user.service.impl;

import com.foodorder.backend.auth.entity.ChangePasswordAttempt;
import com.foodorder.backend.auth.entity.ForgotPasswordRequest;
import com.foodorder.backend.auth.repository.ChangePasswordAttemptRepository;
import com.foodorder.backend.auth.repository.ForgotPasswordRequestRepository;
import com.foodorder.backend.user.dto.request.ChangePasswordRequest;
import com.foodorder.backend.user.dto.request.UserUpdateRequest;
import com.foodorder.backend.auth.entity.UserToken;
import com.foodorder.backend.auth.entity.UserTokenType;
import com.foodorder.backend.auth.repository.UserTokenRepository;
import com.foodorder.backend.user.dto.response.UserResponse;
import com.foodorder.backend.exception.BadRequestException;
import com.foodorder.backend.exception.ResourceNotFoundException;
import com.foodorder.backend.exception.TooManyRequestException;
import com.foodorder.backend.service.BrevoEmailService;
import com.foodorder.backend.service.S3Service;
import com.foodorder.backend.user.repository.UserRepository;
import com.foodorder.backend.user.service.UserService;
import com.foodorder.backend.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Autowired
    // private PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserTokenRepository userTokenRepository;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private BrevoEmailService brevoEmailService;

    @Value("${app.frontend.reset-password-url}")
    private String resetPasswordUrl;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private ForgotPasswordRequestRepository forgotPasswordRequestRepository;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private ChangePasswordAttemptRepository changePasswordAttemptRepository;

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public UserResponse updateProfile(User user, UserUpdateRequest request) {
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setAddress(request.getAddress());
        user.setAvatarUrl(request.getAvatarUrl());

        User updated = userRepository.save(user);
        return UserResponse.fromEntity(updated);
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND"));

        // 1. Upload ảnh mới
        String imageUrl;
        try {
            imageUrl = s3Service.uploadFile(file);
        } catch (IOException e) {
            throw new BadRequestException("UPLOAD_FAILED", "UPLOAD_FAILED");
        }

        // 2. Xoá ảnh cũ nếu có (chỉ khi ảnh mới đã upload thành công)
        String oldAvatarUrl = user.getAvatarUrl();
        if (oldAvatarUrl != null && !oldAvatarUrl.isBlank()) {
            try {
                s3Service.deleteFile(oldAvatarUrl);
            } catch (Exception e) {
                System.err.println("Failed to delete old avatar: " + e.getMessage());
            }
        }

        // 3. Cập nhật vào user
        user.setAvatarUrl(imageUrl);
        userRepository.save(user);

        return imageUrl;
    }

    // Phương thức này sẽ gửi email đặt lại mật khẩu
    @Override
    public void initiatePasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("EMAIL_NOT_FOUND"));

        // Giới hạn 2 lần trong 1 giờ
        int count = forgotPasswordRequestRepository.countByEmailAndRequestedAtAfter(
                email, LocalDateTime.now().minusHours(1));
        if (count >= 2) {
            throw new TooManyRequestException("TOO_MANY_REQUESTS_RESET_PASSWORD");
        }

        // Vô hiệu hoá token cũ
        userTokenRepository.invalidateAllByUserAndType(user, UserTokenType.PASSWORD_RESET);

        // Tạo token mới
        String token = UUID.randomUUID().toString();
        UserToken resetToken = new UserToken();
        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setType(UserTokenType.PASSWORD_RESET);
        resetToken.setCreatedAt(LocalDateTime.now());
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(1));
        userTokenRepository.save(resetToken);

        // Tạo nội dung mail
        String link = resetPasswordUrl + token;
        Context context = new Context();
        context.setVariable("resetLink", link);
        context.setVariable("userEmail", user.getEmail());
        String htmlContent = templateEngine.process("reset_password_email.html", context);

        brevoEmailService.sendEmail(user.getEmail(), "Đặt lại mật khẩu", htmlContent);

        // Ghi log
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null)
            ip = request.getRemoteAddr();
        ForgotPasswordRequest log = new ForgotPasswordRequest();
        log.setEmail(email);
        log.setIpAddress(ip);
        forgotPasswordRequestRepository.save(log);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        UserToken resetToken = userTokenRepository
                .findByTokenAndUsedFalseAndType(token, UserTokenType.PASSWORD_RESET)
                .orElseThrow(() -> new ResourceNotFoundException("INVALID_TOKEN"));

        if (resetToken.getCreatedAt().isBefore(LocalDateTime.now().minusHours(1))) {
            throw new BadRequestException("Token expired", "TOKEN_EXPIRED");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        userTokenRepository.save(resetToken);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest changeRequest) {
        // Kiểm tra người dùng có tồn tại không
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND"));

        // Giới hạn 2 lần đổi mật khẩu trong 1 giờ
        int count = changePasswordAttemptRepository.countByUserIdAndAttemptedAtAfter(
                userId, LocalDateTime.now().minusHours(1));
        if (count >= 2) {
            throw new TooManyRequestException("TOO_MANY_REQUESTS_CHANGE_PASSWORD");
        }

        if (!passwordEncoder.matches(changeRequest.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid current password", "INVALID_CURRENT_PASSWORD");
        }

        user.setPassword(passwordEncoder.encode(changeRequest.getNewPassword()));
        userRepository.save(user);

        // Lưu log đổi mật khẩu
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null)
            ip = request.getRemoteAddr();

        ChangePasswordAttempt attempt = new ChangePasswordAttempt();
        attempt.setUserId(userId);
        attempt.setIpAddress(ip);
        changePasswordAttemptRepository.save(attempt);
    }

    @Override
    public User findUserWithRoleById(Long userId) {
        return userRepository.findUserWithRoleById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND"));
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND"));
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findUserWithRoleByUsername(username)
                .orElse(null); // Trả về null thay vì throw exception để WebSocket controller xử lý
    }
}