package com.foodorder.backend.service.impl;

import com.foodorder.backend.dto.request.UserLoginRequest;
import com.foodorder.backend.dto.request.UserRegisterRequest;
import com.foodorder.backend.dto.response.UserResponse;
import com.foodorder.backend.entity.User;
import com.foodorder.backend.repository.UserRepository;
import com.foodorder.backend.security.JwtUtil;
import com.foodorder.backend.service.EmailService;
import com.foodorder.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    private EmailService emailService;

    // Tạo mã xác nhận ngẫu nhiên
    String token = UUID.randomUUID().toString();

    @Override
    public UserResponse registerUser(UserRegisterRequest request) {
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
                .build();

        userRepository.save(user);

        // Gửi email xác nhận
        emailService.sendVerificationEmail(user.getEmail(), token);

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

        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .token(token)
                .build();
    }


}
