package com.foodorder.backend.controller;

import com.foodorder.backend.dto.request.*;
import com.foodorder.backend.dto.request.ForgotPasswordRequest;
import com.foodorder.backend.dto.response.UserResponse;
import com.foodorder.backend.entity.*;
import com.foodorder.backend.repository.UserRepository;
import com.foodorder.backend.repository.UserTokenRepository;
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

    @Value("${app.frontend.reset-password-url}")
    private String resetPasswordRedirectUrl;

    @Autowired
    private UserTokenRepository userTokenRepository;


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
        Optional<UserToken> tokenOpt = userTokenRepository.findByTokenAndUsedFalseAndType(token, UserTokenType.EMAIL_VERIFICATION);
        if (tokenOpt.isEmpty()) {
            return "verify_failed";
        }

        UserToken userToken = tokenOpt.get();

        // Kiểm tra hết hạn (dùng createdAt hoặc expiresAt đều được, em dùng createdAt như logic cũ)
        if (userToken.getCreatedAt().isBefore(LocalDateTime.now().minusHours(24))) {
            return "verify_failed";
        }

        // Đánh dấu token đã dùng
        userToken.setUsed(true);
        userTokenRepository.save(userToken);

        // Cập nhật user
        User user = userToken.getUser();
        user.setVerified(true);
        userRepository.save(user);

        return "verify_success";
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
        Optional<UserToken> tokenOpt = userTokenRepository
                .findByTokenAndUsedFalseAndType(token, UserTokenType.PASSWORD_RESET);

        if (tokenOpt.isEmpty() || tokenOpt.get().getCreatedAt().isBefore(LocalDateTime.now().minusHours(1))) {
            return "verify_failed";
        }

        model.addAttribute("token", token);
        return "reset_redirect"; // Trả về template trung gian để kiểm tra Token xem có hợp lệ không rồi chuyển tiêsp đến
        // giao diên để người dùng có thể đặt lại mật khẩu

    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody Map<String, String> request) {
        String email = request.get("email");

        try {
            authService.resendVerificationEmail(email);
            return ResponseEntity.ok(Map.of(
                    "status", 200,
                    "message", "Verification email resent successfully"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", 400,
                    "message", e.getMessage()
            ));
        }
    }


}

