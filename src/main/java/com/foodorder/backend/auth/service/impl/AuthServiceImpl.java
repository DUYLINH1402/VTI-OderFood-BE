package com.foodorder.backend.auth.service.impl;

import com.foodorder.backend.auth.dto.request.GoogleLoginRequest;
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
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Collections;
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

    /**
     * Google Client ID để verify ID Token từ Google OAuth
     */
    @Value("${google.client-id}")
    private String googleClientId;

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

    /**
     * Đăng nhập bằng Google OAuth 2.0
     * Quy trình:
     * 1. Verify ID Token với Google
     * 2. Lấy thông tin user từ token (email, name, avatar)
     * 3. Kiểm tra user đã tồn tại chưa:
     *    - Chưa có: Tạo mới user với authProvider = GOOGLE
     *    - Có rồi: Cập nhật thông tin (avatar, tên)
     * 4. Tạo JWT token nội bộ và trả về
     */
    @Override
    public UserResponse loginWithGoogle(GoogleLoginRequest request) {
        // Bước 1: Verify ID Token với Google
        GoogleIdToken.Payload payload = verifyGoogleToken(request.getIdToken());

        String email = payload.getEmail();
        String fullName = (String) payload.get("name");
        String avatarUrl = (String) payload.get("picture");
        boolean emailVerified = payload.getEmailVerified();

        if (!emailVerified) {
            throw new BadRequestException("Email Google chưa được xác minh", "GOOGLE_EMAIL_NOT_VERIFIED");
        }

        // Bước 2: Kiểm tra user đã tồn tại chưa
        Optional<User> existingUserOpt = userRepository.findByEmail(email);
        User user;

        if (existingUserOpt.isPresent()) {
            // User đã tồn tại → Cập nhật thông tin
            user = existingUserOpt.get();

            // Kiểm tra tài khoản có bị khóa không
            if (!user.isActive()) {
                throw new BadRequestException("Tài khoản đã bị khóa", "USER_LOCKED");
            }

            // Cập nhật thông tin từ Google (avatar, tên nếu chưa có)
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                user.setAvatarUrl(avatarUrl);
            }
            if (fullName != null && !fullName.isEmpty() && user.getFullName() == null) {
                user.setFullName(fullName);
            }

            // Nếu user chưa verify email nhưng đăng nhập Google thành công → verify luôn
            // Vì Google đã xác minh email rồi
            if (!user.isVerified()) {
                user.setVerified(true);
            }

            // Cập nhật authProvider nếu user đăng ký bằng email thông thường trước đó
            if (!"GOOGLE".equals(user.getAuthProvider())) {
                user.setAuthProvider("GOOGLE");
            }

            // Cập nhật thời gian đăng nhập
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

        } else {
            // User chưa tồn tại → Tạo mới
            user = createGoogleUser(email, fullName, avatarUrl);
        }

        // Bước 3: Tạo JWT token nội bộ
        String jwtToken = jwtUtil.generateToken(user);

        // Bước 4: Trả về response
        UserResponse response = UserResponse.fromEntity(user);
        response.setToken(jwtToken);
        return response;
    }

    /**
     * Verify ID Token với Google Server
     * @param idToken ID Token từ Google OAuth
     * @return Payload chứa thông tin user
     */
    private GoogleIdToken.Payload verifyGoogleToken(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);

            if (googleIdToken == null) {
                throw new BadRequestException("Token Google không hợp lệ hoặc đã hết hạn", "INVALID_GOOGLE_TOKEN");
            }

            return googleIdToken.getPayload();

        } catch (BadRequestException e) {
            throw e;
        } catch (Exception e) {
            throw new BadRequestException("Không thể xác thực token Google: " + e.getMessage(), "GOOGLE_VERIFICATION_FAILED");
        }
    }

    /**
     * Tạo mới user từ thông tin Google
     * @param email Email từ Google
     * @param fullName Tên đầy đủ từ Google
     * @param avatarUrl URL avatar từ Google
     * @return User mới được tạo
     */
    private User createGoogleUser(String email, String fullName, String avatarUrl) {
        // Tạo username từ email (lấy phần trước @)
        String baseUsername = email.split("@")[0];
        String username = baseUsername;

        // Kiểm tra username đã tồn tại chưa, nếu có thì thêm số random
        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        // Lấy role CUSTOMER từ database
        Role customerRole = roleRepository.findByCode("ROLE_USER")
                .orElseThrow(() -> new BadRequestException("Role mặc định không tồn tại", "ROLE_NOT_FOUND"));

        // Tạo mật khẩu random (user không cần dùng vì đăng nhập qua Google)
        String randomPassword = UUID.randomUUID().toString();

        User newUser = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(randomPassword))
                .fullName(fullName)
                .avatarUrl(avatarUrl)
                .authProvider("GOOGLE")
                .isActive(true)
                .isVerified(true) // Google đã xác minh email
                .role(customerRole)
                .lastLogin(LocalDateTime.now())
                .build();

        userRepository.save(newUser);

        // Tạo RewardPoint cho user mới với balance = 10000
        RewardPoint rewardPoint = RewardPoint.builder()
                .user(newUser)
                .balance(10000)
                .lastUpdated(LocalDateTime.now())
                .build();
        rewardPointRepository.save(rewardPoint);

        // Lưu log vào point_history khi tạo RewardPoint cho user mới
        PointHistory pointHistory = PointHistory.builder()
                .userId(newUser.getId())
                .type(PointType.EARN)
                .amount(10000)
                .orderId(null)
                .description("Tặng điểm khi đăng ký qua Google")
                .createdAt(LocalDateTime.now())
                .build();
        pointHistoryRepository.save(pointHistory);

        return newUser;
    }

}
