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
     *
     * QUAN TRỌNG:
     * - KHÔNG ghi đè mật khẩu cũ để user vẫn có thể đăng nhập bằng email/password
     * - Chỉ cập nhật avatar, fullName nếu chưa có (ưu tiên dữ liệu user đã tự cập nhật)
     * - Cập nhật authProvider để cho phép đăng nhập bằng nhiều cách
     */
    private User updateExistingUser(User user, String fullName, String avatarUrl, String authProvider) {
        // Chỉ cập nhật avatar nếu user chưa có avatar (ưu tiên avatar user tự upload)
        if ((user.getAvatarUrl() == null || user.getAvatarUrl().isEmpty())
                && avatarUrl != null && !avatarUrl.isEmpty()) {
            user.setAvatarUrl(avatarUrl);
        }

        // Chỉ cập nhật tên nếu user chưa có fullName (ưu tiên tên user tự cập nhật)
        if ((user.getFullName() == null || user.getFullName().isEmpty())
                && fullName != null && !fullName.isEmpty()) {
            user.setFullName(fullName);
        }

        // Xác minh email nếu chưa (vì provider đã xác minh)
        if (!user.isVerified()) {
            user.setVerified(true);
        }

        // Cập nhật authProvider để ghi nhận user đã liên kết với OAuth2
        // Nhưng KHÔNG ghi đè mật khẩu cũ - user vẫn có thể đăng nhập bằng email/password
        // Format: "LOCAL,GOOGLE" hoặc "LOCAL,FACEBOOK" hoặc "LOCAL,GOOGLE,FACEBOOK"
        String currentProvider = user.getAuthProvider();
        if (currentProvider == null || currentProvider.isEmpty()) {
            currentProvider = "LOCAL";
        }

        // Thêm provider mới nếu chưa có
        if (!currentProvider.contains(authProvider)) {
            user.setAuthProvider(currentProvider + "," + authProvider);
            log.info("User {} đã liên kết thêm provider: {}", user.getEmail(), authProvider);
        }

        // Cập nhật thời gian đăng nhập
        user.setLastLogin(LocalDateTime.now());

        log.info("User đăng nhập qua {} OAuth2: {} (providers: {})",
                authProvider, user.getEmail(), user.getAuthProvider());
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

