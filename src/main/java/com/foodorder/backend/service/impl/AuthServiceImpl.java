package com.foodorder.backend.service.impl;

import com.foodorder.backend.dto.request.UserLoginRequest;
import com.foodorder.backend.dto.request.UserRegisterRequest;
import com.foodorder.backend.dto.response.UserResponse;
import com.foodorder.backend.entity.User;
import com.foodorder.backend.repository.UserRepository;
import com.foodorder.backend.security.JwtUtil;
import com.foodorder.backend.service.BrevoEmailService;
import com.foodorder.backend.service.AuthService;
import com.foodorder.backend.service.ThymeleafTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private final JwtUtil jwtUtil;
    @Autowired
    private final BrevoEmailService brevoEmailService;

    @Autowired
    private final ThymeleafTemplateService thymeleafTemplateService;


    @Override
    public UserResponse registerUser(UserRegisterRequest request) {
        // Tạo mã xác nhận ngẫu nhiên
        String token = UUID.randomUUID().toString();


        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("USERNAME_ALREADY_EXISTS");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("EMAIL_ALREADY_EXISTS");
        }

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .isVerified(false)
                .role("ROLE_USER")
                .point(0)
                .verificationToken(token)
                .verificationTokenCreatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        // Gửi email xác nhận
        try {
            String html = thymeleafTemplateService.buildVerificationEmail(user.getFullName(), token);
            brevoEmailService.sendEmail(user.getEmail(), "Xác nhận tài khoản Dong Xanh", html);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }


    @Override
    public UserResponse loginUser(UserLoginRequest request) {
        String loginInput = request.getLogin();

        // Tìm người dùng theo email hoặc username
        Optional<User> userOpt = loginInput.contains("@")
                ? userRepository.findByEmail(loginInput)
                : userRepository.findByUsername(loginInput);

        User user = userOpt.orElseThrow(() -> new RuntimeException("USER_NOT_FOUND"));

        // Kiểm tra mật khẩu
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("INVALID_CREDENTIALS");
        }

        // Kiểm tra tài khoản có bị khóa không
        if (!user.isActive())
        {
            throw new RuntimeException("USER_LOCKED");
        }

        // Kiểm tra xác minh email
        if (!user.isVerified())
        {
            throw new RuntimeException("EMAIL_NOT_VERIFIED");
        }

        // Sinh JWT
        String token = jwtUtil.generateToken(user);

        //  Cập nhật thời gian đăng nhập
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        //  Dùng fromEntity cho đồng bộ
        UserResponse response = UserResponse.fromEntity(user);
        response.setToken(token); // Gắn token vào response
        return response;

    }





}
