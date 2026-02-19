package com.foodorder.backend.user.service.impl;

import com.foodorder.backend.auth.entity.UserToken;
import com.foodorder.backend.auth.entity.UserTokenType;
import com.foodorder.backend.auth.repository.UserTokenRepository;
import com.foodorder.backend.config.RestPage;
import com.foodorder.backend.exception.BadRequestException;
import com.foodorder.backend.exception.ForbiddenException;
import com.foodorder.backend.exception.ResourceNotFoundException;
import com.foodorder.backend.security.CustomUserDetails;
import com.foodorder.backend.service.BrevoEmailService;
import com.foodorder.backend.user.dto.request.AdminCreateUserRequest;
import com.foodorder.backend.user.dto.request.AdminUpdateUserRequest;
import com.foodorder.backend.user.dto.request.AdminUpdateUserStatusRequest;
import com.foodorder.backend.user.dto.response.AdminUserResponse;
import com.foodorder.backend.user.entity.Role;
import com.foodorder.backend.user.entity.User;
import com.foodorder.backend.user.repository.RoleRepository;
import com.foodorder.backend.user.repository.UserRepository;
import com.foodorder.backend.user.service.AdminUserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.foodorder.backend.config.CacheConfig.*;

/**
 * Service implementation cho admin quản lý user
 */
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserTokenRepository userTokenRepository;
    private final BrevoEmailService brevoEmailService;
    private final TemplateEngine templateEngine;

    @Value("${app.frontend.reset-password-url}")
    private String resetPasswordUrl;

    // ==================== Helper Methods ====================

    /**
     * Lấy thông tin user hiện tại từ SecurityContext
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getUser();
        }
        return null;
    }

    /**
     * Kiểm tra user hiện tại có phải là SUPER_ADMIN không
     */
    private boolean isCurrentUserSuperAdmin() {
        User currentUser = getCurrentUser();
        return currentUser != null && currentUser.isSuperAdmin();
    }

    /**
     * Kiểm tra quyền thao tác trên dữ liệu được bảo vệ
     * Nếu dữ liệu được bảo vệ (isProtected = true) và user không phải SUPER_ADMIN, throw ForbiddenException
     */
    private void checkProtectedDataPermission(boolean isProtected, String action) {
        if (isProtected && !isCurrentUserSuperAdmin()) {
            log.warn("User không có quyền {} dữ liệu được bảo vệ", action);
            throw new ForbiddenException(
                    "Dữ liệu được bảo vệ, chỉ Super Admin mới có quyền " + action,
                    "PROTECTED_DATA_ACCESS_DENIED"
            );
        }
    }

    @Override
    @Cacheable(value = ADMIN_USERS_CACHE, key = "#keyword + '_' + #roleCode + '_' + #isActive + '_' + #pageable.pageNumber + '_' + #pageable.pageSize + '_' + #pageable.sort.toString()")
    public Page<AdminUserResponse> getAllUsers(String keyword, String roleCode, Boolean isActive, Pageable pageable) {
        log.info("Fetching users from database (not cached)");
        Page<User> users = userRepository.findAllUsersWithFilters(keyword, roleCode, isActive, pageable);
        Page<AdminUserResponse> page = users.map(AdminUserResponse::fromEntity);
        return new RestPage<>(page.getContent(), page.getPageable(), page.getTotalElements());
    }

    @Override
    @Cacheable(value = ADMIN_EMPLOYEES_CACHE, key = "#roleCode + '_' + #keyword + '_' + #isActive + '_' + #pageable.pageNumber + '_' + #pageable.pageSize + '_' + #pageable.sort.toString()")
    public Page<AdminUserResponse> getUsersByRole(String roleCode, String keyword, Boolean isActive, Pageable pageable) {
        log.info("Fetching users by role from database (not cached)");
        // Validate roleCode
        if (!roleRepository.existsByCode(roleCode)) {
            throw new BadRequestException("Role không tồn tại", "ROLE_NOT_FOUND");
        }

        Page<User> users = userRepository.findAllUsersWithFilters(keyword, roleCode, isActive, pageable);
        Page<AdminUserResponse> page = users.map(AdminUserResponse::fromEntity);
        return new RestPage<>(page.getContent(), page.getPageable(), page.getTotalElements());
    }

    @Override
    @Cacheable(value = ADMIN_USER_DETAILS_CACHE, key = "#userId")
    public AdminUserResponse getUserById(Long userId) {
        log.info("Fetching user details from database (not cached) - userId: {}", userId);
        User user = userRepository.findUserWithRoleById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng", "USER_NOT_FOUND"));

        return AdminUserResponse.fromEntity(user);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = ADMIN_USERS_CACHE, allEntries = true),
            @CacheEvict(value = ADMIN_EMPLOYEES_CACHE, allEntries = true)
    })
    public AdminUserResponse createUser(AdminCreateUserRequest request) {

        // Kiểm tra username đã tồn tại
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username đã tồn tại", "USERNAME_EXISTS");
        }

        // Kiểm tra email đã tồn tại
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email đã được sử dụng", "EMAIL_EXISTS");
        }

        // Lấy role, mặc định là ROLE_USER nếu không chỉ định
        String roleCode = request.getRoleCode() != null ? request.getRoleCode() : "ROLE_USER";
        Role role = roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new BadRequestException("Role không tồn tại", "ROLE_NOT_FOUND"));

        // Tạo user mới
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .avatarUrl(request.getAvatarUrl())
                .role(role)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .isVerified(request.getIsVerified() != null ? request.getIsVerified() : true) // Admin tạo thì mặc định đã verified
                .build();

        User savedUser = userRepository.save(user);

        return AdminUserResponse.fromEntity(savedUser);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = ADMIN_USERS_CACHE, allEntries = true),
            @CacheEvict(value = ADMIN_EMPLOYEES_CACHE, allEntries = true),
            @CacheEvict(value = ADMIN_USER_DETAILS_CACHE, key = "#userId")
    })
    public AdminUserResponse updateUser(Long userId, AdminUpdateUserRequest request) {

        User user = userRepository.findUserWithRoleById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng", "USER_NOT_FOUND"));

        // Kiểm tra quyền nếu dữ liệu được bảo vệ
        checkProtectedDataPermission(user.isProtected(), "cập nhật");

        // Kiểm tra username nếu thay đổi
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new BadRequestException("Username đã tồn tại", "USERNAME_EXISTS");
            }
            user.setUsername(request.getUsername());
        }

        // Kiểm tra email nếu thay đổi
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email đã được sử dụng", "EMAIL_EXISTS");
            }
            user.setEmail(request.getEmail());
        }

        // Cập nhật mật khẩu nếu có
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // Cập nhật các thông tin khác
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }

        // Cập nhật role nếu có
        if (request.getRoleCode() != null && !request.getRoleCode().equals(user.getRole().getCode())) {
            Role newRole = roleRepository.findByCode(request.getRoleCode())
                    .orElseThrow(() -> new BadRequestException("Role không tồn tại", "ROLE_NOT_FOUND"));
            user.setRole(newRole);
        }

        // Cập nhật trạng thái
        if (request.getIsActive() != null) {
            user.setActive(request.getIsActive());
        }
        if (request.getIsVerified() != null) {
            user.setVerified(request.getIsVerified());
        }

        User updatedUser = userRepository.save(user);

        return AdminUserResponse.fromEntity(updatedUser);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = ADMIN_USERS_CACHE, allEntries = true),
            @CacheEvict(value = ADMIN_EMPLOYEES_CACHE, allEntries = true),
            @CacheEvict(value = ADMIN_USER_DETAILS_CACHE, key = "#userId")
    })
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng", "USER_NOT_FOUND"));

        // Kiểm tra quyền nếu dữ liệu được bảo vệ
        checkProtectedDataPermission(user.isProtected(), "xóa");

        // Không cho phép xóa admin
        if (user.isAdmin()) {
            throw new BadRequestException("Không thể xóa tài khoản admin", "CANNOT_DELETE_ADMIN");
        }

        userRepository.delete(user);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = ADMIN_USERS_CACHE, allEntries = true),
            @CacheEvict(value = ADMIN_EMPLOYEES_CACHE, allEntries = true),
            @CacheEvict(value = ADMIN_USER_DETAILS_CACHE, key = "#userId")
    })
    public AdminUserResponse updateUserStatus(Long userId, AdminUpdateUserStatusRequest request) {
        User user = userRepository.findUserWithRoleById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng", "USER_NOT_FOUND"));

        // Kiểm tra quyền nếu dữ liệu được bảo vệ
        checkProtectedDataPermission(user.isProtected(), "cập nhật trạng thái");

        // Không cho phép khóa admin
        if (user.isAdmin() && !request.getIsActive()) {
            throw new BadRequestException("Không thể khóa tài khoản admin", "CANNOT_LOCK_ADMIN");
        }
        user.setActive(request.getIsActive());
        User updatedUser = userRepository.save(user);
        return AdminUserResponse.fromEntity(updatedUser);
    }

    /**
     * Admin gửi email reset mật khẩu cho user (không giới hạn số lần)
     *
     * @param userId ID của user cần reset mật khẩu
     */
    @Override
    public void sendResetPasswordEmail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng", "USER_NOT_FOUND"));

        // Không cho phép reset mật khẩu admin
        if (user.isAdmin()) {
            throw new BadRequestException("Không thể reset mật khẩu tài khoản admin", "CANNOT_RESET_ADMIN_PASSWORD");
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

        // Gửi email
        brevoEmailService.sendEmail(user.getEmail(), "Đặt lại mật khẩu", htmlContent);

        log.info("Admin đã gửi email reset mật khẩu cho user: {} (ID: {})", user.getEmail(), userId);
    }
}
