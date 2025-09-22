package com.foodorder.backend.auth.service.impl;

import com.foodorder.backend.auth.dto.request.UserLoginRequest;
import com.foodorder.backend.exception.BadRequestException;
import com.foodorder.backend.auth.dto.request.UserRegisterRequest;
import com.foodorder.backend.points.repository.PointHistoryRepository;
import com.foodorder.backend.user.dto.response.UserResponse;
import com.foodorder.backend.user.entity.Role;
import com.foodorder.backend.user.entity.User;
import com.foodorder.backend.auth.entity.UserToken;
import com.foodorder.backend.auth.entity.UserTokenType;
import com.foodorder.backend.user.repository.UserRepository;
import com.foodorder.backend.user.repository.RoleRepository;
import com.foodorder.backend.auth.repository.UserTokenRepository;
import com.foodorder.backend.security.JwtUtil;
import com.foodorder.backend.service.BrevoEmailService;
import com.foodorder.backend.auth.service.AuthService;
import com.foodorder.backend.service.ThymeleafTemplateService;
import com.foodorder.backend.points.entity.RewardPoint;
import com.foodorder.backend.points.entity.PointHistory;
import com.foodorder.backend.points.entity.PointType;
import com.foodorder.backend.points.repository.RewardPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final PointHistoryRepository pointHistoryRepository;
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtil jwtUtil;

    private final BrevoEmailService brevoEmailService;

    private final ThymeleafTemplateService thymeleafTemplateService;

    private final UserTokenRepository userTokenRepository;

    private final RewardPointRepository rewardPointRepository;

    private final RoleRepository roleRepository;

    @Override
    public UserResponse registerUser(UserRegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists", "USERNAME_ALREADY_EXISTS");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists", "EMAIL_ALREADY_EXISTS");
        }

        // Lấy role CUSTOMER từ database
        Role customerRole = roleRepository.findByCode("ROLE_USER")
                .orElseThrow(() -> new BadRequestException("Default role not found", "ROLE_NOT_FOUND"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .isVerified(false)
                .role(customerRole) // Sử dụng entity Role thay vì enum
                .build();

        userRepository.save(user);

        // Tạo RewardPoint cho user mới với balance = 10000
        RewardPoint rewardPoint = RewardPoint.builder()
                .user(user)
                .balance(10000)
                .lastUpdated(LocalDateTime.now())
                .build();
        rewardPointRepository.save(rewardPoint);

        // Lưu log vào point_history khi tạo RewardPoint cho user mới
        PointHistory pointHistory = PointHistory
                .builder()
                .userId(user.getId())
                .type(PointType.EARN)
                .amount(10000)
                .orderId(null)
                .description("Tặng điểm khi đăng ký")
                .createdAt(LocalDateTime.now())
                .build();
        pointHistoryRepository.save(pointHistory);

        // Invalidate old tokens
        userTokenRepository.invalidateAllByUserAndType(user, UserTokenType.EMAIL_VERIFICATION);

        // Create new token
        String token = UUID.randomUUID().toString();
        UserToken userToken = new UserToken();
        userToken.setUser(user);
        userToken.setToken(token);
        userToken.setType(UserTokenType.EMAIL_VERIFICATION);
        userToken.setCreatedAt(LocalDateTime.now());
        userToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        userTokenRepository.save(userToken);

        // Send email
        try {
            String name = user.getFullName() != null ? user.getFullName() : user.getUsername();
            String html = thymeleafTemplateService.buildVerificationEmail(name, token);
            brevoEmailService.sendEmail(user.getEmail(), "Xác nhận tài khoản Dong Xanh", html);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roleCode(user.getRoleCode()) // Trả về authority string
                .roleName(user.getRoleDisplayName()) // Trả về tên hiển thị của role
                .token(null)
                .point(rewardPoint.getBalance())
                .isActive(user.isActive())
                .isVerified(user.isVerified())
                .build();
    }

    @Override
    public UserResponse loginUser(UserLoginRequest request) {
        String loginInput = request.getLogin();

        // Tìm người dùng theo email hoặc username
        Optional<User> userOpt = loginInput.contains("@")
                ? userRepository.findByEmail(loginInput)
                : userRepository.findByUsername(loginInput);

        User user = userOpt.orElseThrow(() -> new BadRequestException("Account not found", "USER_NOT_FOUND"));

        // Kiểm tra mật khẩu
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid password", "INVALID_CREDENTIALS");
        }

        // Kiểm tra tài khoản có bị khóa không
        if (!user.isActive()) {
            throw new BadRequestException("Account is locked", "USER_LOCKED");
        }

        // Kiểm tra xác minh email
        if (!user.isVerified()) {
            throw new BadRequestException("Email not verified", "EMAIL_NOT_VERIFIED");
        }

        // Sinh JWT
        String token = jwtUtil.generateToken(user);

        // Cập nhật thời gian đăng nhập
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Dùng fromEntity cho đồng bộ
        UserResponse response = UserResponse.fromEntity(user);
        response.setToken(token); // Gắn token vào response
        return response;

    }

    // GỬI LẠI EMAIL XÁC MINH
    @Override
    public void resendVerificationEmail(String emailOrUsername) {
        // Cho phép nhập username hoặc email
        User user = userRepository.findByEmail(emailOrUsername)
                .or(() -> userRepository.findByUsername(emailOrUsername))
                .orElseThrow(() -> new BadRequestException("Email not found", "EMAIL_NOT_FOUND"));

        if (user.isVerified()) {
            throw new BadRequestException("Email already verified", "EMAIL_ALREADY_VERIFIED");
        }

        // Giới hạn gửi lại 3 lần trong 1 giờ
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long count = userTokenRepository.countRecentTokens(user.getEmail(), UserTokenType.EMAIL_VERIFICATION,
                oneHourAgo);
        if (count >= 3) {
            throw new BadRequestException("Too many requests to resend verification email",
                    "TOO_MANY_REQUESTS_RESEND_VERIFICATION");
        }

        // Vô hiệu hoá token cũ
        userTokenRepository.invalidateAllByUserAndType(user, UserTokenType.EMAIL_VERIFICATION);

        // Tạo token mới
        String token = UUID.randomUUID().toString();
        UserToken newToken = new UserToken();
        newToken.setUser(user);
        newToken.setToken(token);
        newToken.setType(UserTokenType.EMAIL_VERIFICATION);
        newToken.setCreatedAt(LocalDateTime.now());
        newToken.setExpiresAt(LocalDateTime.now().plusHours(24));
        userTokenRepository.save(newToken);

        // Gửi lại email xác minh
        try {
            String name = user.getFullName() != null ? user.getFullName() : user.getUsername();
            String html = thymeleafTemplateService.buildVerificationEmail(name, token);
            brevoEmailService.sendEmail(user.getEmail(), "Xác nhận lại tài khoản Dong Xanh", html);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
