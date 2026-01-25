package com.foodorder.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Cấu hình PasswordEncoder riêng biệt
 * Tách ra khỏi SecurityConfig để tránh circular dependency
 * với OAuth2LoginSuccessHandler
 */
@Configuration
public class PasswordEncoderConfig {

    /**
     * Bean PasswordEncoder sử dụng BCrypt để mã hóa mật khẩu
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

