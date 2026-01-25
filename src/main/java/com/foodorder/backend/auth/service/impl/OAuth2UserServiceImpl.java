package com.foodorder.backend.auth.service.impl;

import com.foodorder.backend.auth.service.OAuth2UserService;
import com.foodorder.backend.exception.BadRequestException;
import com.foodorder.backend.points.entity.PointHistory;
import com.foodorder.backend.points.entity.PointType;
import com.foodorder.backend.points.entity.RewardPoint;
import com.foodorder.backend.points.repository.PointHistoryRepository;
import com.foodorder.backend.points.repository.RewardPointRepository;
import com.foodorder.backend.security.JwtUtil;
import com.foodorder.backend.user.entity.Role;
import com.foodorder.backend.user.entity.User;
import com.foodorder.backend.user.repository.RoleRepository;
import com.foodorder.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation của OAuth2UserService
 * Xử lý OAuth2 user trong transaction để tránh LazyInitializationException
 * Hỗ trợ nhiều provider: Google, Facebook
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2UserServiceImpl implements OAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RewardPointRepository rewardPointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    /**
     * Xử lý OAuth2 user và tạo JWT token trong một transaction
     * Đảm bảo Role entity được load đúng cách trước khi tạo token
     *
     * @param authProvider Provider xác thực (GOOGLE, FACEBOOK)
     */
    @Override
    @Transactional
    public String processOAuth2UserAndGenerateToken(String email, String fullName, String avatarUrl, String authProvider) {
        // Xử lý user: tìm hoặc tạo mới
        User user = processOAuth2User(email, fullName, avatarUrl, authProvider);

        // Tạo JWT token (Role đã được load trong transaction)
        return jwtUtil.generateToken(user);
    }

    /**
     * Xử lý user từ OAuth2: tìm user hiện có hoặc tạo mới
     */
    private User processOAuth2User(String email, String fullName, String avatarUrl, String authProvider) {
        Optional<User> existingUserOpt = userRepository.findByEmail(email);

        if (existingUserOpt.isPresent()) {
            return updateExistingUser(existingUserOpt.get(), fullName, avatarUrl, authProvider);
        } else {
            return createNewOAuth2User(email, fullName, avatarUrl, authProvider);
        }
    }

    /**
     * Cập nhật thông tin user đã tồn tại từ OAuth2 provider
     * Luôn cập nhật avatar và fullName từ provider (ưu tiên dữ liệu mới)
     */
    private User updateExistingUser(User user, String fullName, String avatarUrl, String authProvider) {
        // Luôn cập nhật avatar từ provider nếu có (dữ liệu mới nhất)
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            user.setAvatarUrl(avatarUrl);
        }

        // Luôn cập nhật tên từ provider nếu có (dữ liệu mới nhất)
        if (fullName != null && !fullName.isEmpty()) {
            user.setFullName(fullName);
        }

        // Xác minh email nếu chưa (vì provider đã xác minh)
        if (!user.isVerified()) {
            user.setVerified(true);
        }

        // Cập nhật authProvider nếu user đăng ký bằng email trước đó hoặc đổi provider
        if (!authProvider.equals(user.getAuthProvider())) {
            user.setAuthProvider(authProvider);
        }

        // Cập nhật thời gian đăng nhập
        user.setLastLogin(LocalDateTime.now());

        log.info("Updated existing user from {} OAuth2: {}", authProvider, user.getEmail());
        return userRepository.save(user);
    }

    /**
     * Tạo mới user từ thông tin OAuth2 provider
     */
    private User createNewOAuth2User(String email, String fullName, String avatarUrl, String authProvider) {
        // Tạo username từ email (lấy phần trước @)
        String baseUsername = email.split("@")[0];
        String username = baseUsername;

        // Kiểm tra username đã tồn tại chưa, nếu có thì thêm số
        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }

        // Lấy role CUSTOMER từ database
        Role customerRole = roleRepository.findByCode("ROLE_USER")
                .orElseThrow(() -> new BadRequestException("Role mặc định không tồn tại", "ROLE_NOT_FOUND"));

        // Tạo mật khẩu random (user không cần dùng vì đăng nhập qua OAuth2)
        String randomPassword = UUID.randomUUID().toString();

        User newUser = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(randomPassword))
                .fullName(fullName)
                .avatarUrl(avatarUrl)
                .authProvider(authProvider)
                .isActive(true)
                .isVerified(true) // Provider đã xác minh email
                .role(customerRole)
                .lastLogin(LocalDateTime.now())
                .build();

        userRepository.save(newUser);
        log.info("Created new {} OAuth2 user: {}", authProvider, email);

        // Tạo RewardPoint cho user mới với balance = 10000
        RewardPoint rewardPoint = RewardPoint.builder()
                .user(newUser)
                .balance(10000)
                .lastUpdated(LocalDateTime.now())
                .build();
        rewardPointRepository.save(rewardPoint);

        // Lưu log vào point_history
        String description = "GOOGLE".equals(authProvider)
                ? "Tặng điểm khi đăng ký qua Google"
                : "Tặng điểm khi đăng ký qua Facebook";

        PointHistory pointHistory = PointHistory.builder()
                .userId(newUser.getId())
                .type(PointType.EARN)
                .amount(10000)
                .orderId(null)
                .description(description)
                .createdAt(LocalDateTime.now())
                .build();
        pointHistoryRepository.save(pointHistory);

        return newUser;
    }
}

