package com.foodorder.backend.controller;

import com.foodorder.backend.dto.request.*;
import com.foodorder.backend.dto.response.UserResponse;
import com.foodorder.backend.entity.PasswordResetToken;
import com.foodorder.backend.entity.User;
import com.foodorder.backend.repository.PasswordResetTokenRepository;
import com.foodorder.backend.repository.UserRepository;
import com.foodorder.backend.service.AuthService;
import com.foodorder.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    @Autowired
    private  AuthService authService;
     @Autowired
    private UserService userService;

    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${app.frontend.reset-password-url}")
    private String resetPasswordRedirectUrl;


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid UserRegisterRequest request) {
        try {
            UserResponse response = authService.registerUser(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "message", e.getMessage()
            ));
        }
    }


    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody @Valid UserLoginRequest request) {
        return ResponseEntity.ok(authService.loginUser(request));
    }

    @GetMapping("/verify")
    public String verifyUser(@RequestParam("token") String token) {
        Optional<User> userOpt = userRepository.findByVerificationToken(token);
        if (userOpt.isEmpty()) {
            return "verify_failed"; // template HTML
        }

        User user = userOpt.get();

        // Kiểm tra hết hạn sau 24 giờ
        if (user.getVerificationTokenCreatedAt().isBefore(LocalDateTime.now().minusHours(24))) {
            return "verify_failed";
        }
        user.setVerified(true);
        user.setVerificationToken(null);
        user.setVerificationTokenCreatedAt(null);
        userRepository.save(user);

        return "verify_success"; // template HTML
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        userService.initiatePasswordReset(request.getEmail());
        return ResponseEntity.ok("RESET_LINK_SENT");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("PASSWORD_RESET_SUCCESS");
    }

    @GetMapping("/reset-password/verify")
    public String verifyResetPassword(@RequestParam("token") String token, Model model) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElse(null);

        if (resetToken == null || resetToken.getCreatedAt().isBefore(LocalDateTime.now().minusHours(24))) {
            return "verify_failed";
        } // Kiểm tra token có hợp lệ và chưa hết hạn

        model.addAttribute("token", token);
        return "reset_redirect"; // Trả về template trung gian để kiểm tra Token xem có hợp lệ không rồi chuyển tiêsp đến
        // giao diên để người dùng có thể đặt lại mật khẩu

    }



}

