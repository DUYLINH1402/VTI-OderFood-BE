package com.foodorder.backend.auth;

import com.foodorder.backend.auth.dto.request.ResetPasswordRequest;
import com.foodorder.backend.auth.dto.request.UserLoginRequest;
import com.foodorder.backend.auth.dto.request.UserRegisterRequest;
import com.foodorder.backend.auth.dto.request.ForgotPasswordRequest;
import com.foodorder.backend.auth.entity.UserToken;
import com.foodorder.backend.auth.entity.UserTokenType;
import com.foodorder.backend.user.dto.response.UserResponse;
import com.foodorder.backend.user.repository.UserRepository;
import com.foodorder.backend.auth.repository.UserTokenRepository;
import com.foodorder.backend.auth.service.AuthService;
import com.foodorder.backend.user.service.UserService;
import com.foodorder.backend.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

/**
 * Controller xử lý các nghiệp vụ xác thực người dùng
 */
@Controller
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API xác thực và quản lý tài khoản người dùng")
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


    @Operation(summary = "Đăng ký tài khoản", description = "Đăng ký tài khoản mới cho người dùng. Sau khi đăng ký thành công, email xác thực sẽ được gửi đến địa chỉ email đã đăng ký.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đăng ký thành công",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ hoặc email/username đã tồn tại")
    })
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


    @Operation(summary = "Đăng nhập", description = "Đăng nhập vào hệ thống bằng email/username và mật khẩu. Trả về JWT token nếu thành công.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đăng nhập thành công",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Thông tin đăng nhập không chính xác"),
            @ApiResponse(responseCode = "403", description = "Tài khoản chưa được xác thực email")
    })
    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody @Valid UserLoginRequest request) {
        return ResponseEntity.ok(authService.loginUser(request));
    }

    @Operation(summary = "Xác thực email", description = "Xác thực email người dùng thông qua token được gửi qua email. Token có hiệu lực trong 24 giờ.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xác thực thành công - Trả về trang verify_success"),
            @ApiResponse(responseCode = "400", description = "Token không hợp lệ hoặc đã hết hạn - Trả về trang verify_failed")
    })
    @GetMapping("/verify")
    public String verifyUser(
            @Parameter(description = "Token xác thực được gửi qua email")
            @RequestParam("token") String token) {
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




    @Operation(summary = "Quên mật khẩu", description = "Gửi email chứa link đặt lại mật khẩu đến địa chỉ email đã đăng ký.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email đặt lại mật khẩu đã được gửi"),
            @ApiResponse(responseCode = "404", description = "Email không tồn tại trong hệ thống")
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        userService.initiatePasswordReset(request.getEmail());
        return ResponseEntity.ok("RESET_LINK_SENT");
    }

    @Operation(summary = "Đặt lại mật khẩu", description = "Đặt lại mật khẩu mới bằng token được gửi qua email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đặt lại mật khẩu thành công"),
            @ApiResponse(responseCode = "400", description = "Token không hợp lệ hoặc đã hết hạn")
    })
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("PASSWORD_RESET_SUCCESS");
    }

    @Operation(summary = "Xác thực token đặt lại mật khẩu", description = "Kiểm tra token đặt lại mật khẩu có hợp lệ không. Token có hiệu lực trong 1 giờ.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token hợp lệ - Chuyển hướng đến trang đặt lại mật khẩu"),
            @ApiResponse(responseCode = "400", description = "Token không hợp lệ hoặc đã hết hạn")
    })
    @GetMapping("/reset-password/verify")
    public String verifyResetPassword(
            @Parameter(description = "Token đặt lại mật khẩu")
            @RequestParam("token") String token, Model model) {
        Optional<UserToken> tokenOpt = userTokenRepository
                .findByTokenAndUsedFalseAndType(token, UserTokenType.PASSWORD_RESET);

        if (tokenOpt.isEmpty() || tokenOpt.get().getCreatedAt().isBefore(LocalDateTime.now().minusHours(1))) {
            return "verify_failed";
        }

        model.addAttribute("token", token);
        return "reset_redirect"; // Trả về template trung gian để kiểm tra Token xem có hợp lệ không rồi chuyển tiêsp đến
        // giao diên để người dùng có thể đặt lại mật khẩu

    }

    // Gửi lại email xác minh
    // Nếu người dùng đã xác minh email thì không cần gửi lại
    // nếu chưa xác minh thì gửi lại email xác minh l
    @Operation(summary = "Gửi lại email xác thực", description = "Gửi lại email xác thực cho người dùng chưa xác minh email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email xác thực đã được gửi lại"),
            @ApiResponse(responseCode = "400", description = "Email không tồn tại hoặc đã được xác thực")
    })
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody Map<String, String> request) {
        String emailOrUsername = request.get("email");

        try {
            authService.resendVerificationEmail(emailOrUsername);
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

