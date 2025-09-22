package com.foodorder.backend.config;

import com.foodorder.backend.security.JwtAccessDeniedHandler;
import com.foodorder.backend.security.JwtAuthenticationEntryPoint;
import com.foodorder.backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Cấu hình Spring Security
 * Đặc biệt cho phép WebSocket endpoints hoạt động và xử lý lỗi JWT toàn cục
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    /**
     * Bean PasswordEncoder sử dụng BCrypt để mã hóa mật khẩu
     * Đây là bean mà AuthServiceImpl cần để inject
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Cấu hình xử lý lỗi authentication và authorization
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint) // Xử lý khi chưa đăng nhập
                        .accessDeniedHandler(jwtAccessDeniedHandler) // Xử lý khi không có quyền
                )

                .authorizeHttpRequests(auth -> auth
                        // Cho phép tất cả WebSocket endpoints
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/app/**").permitAll()     // STOMP destination prefix
                        .requestMatchers("/topic/**").permitAll()   // Message broker topics
                        .requestMatchers("/queue/**").permitAll()
                        .requestMatchers("/ws/staff-orders/**").permitAll()

                        // Chatbot AI
                        .requestMatchers("/api/chatbot/**").permitAll()

                        // Cho phép static resources và test pages
                        .requestMatchers("/static/**", "/websocket-test.html", "/*.html").permitAll()

                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/foods/**").permitAll()
                        .requestMatchers("/api/categories/**").permitAll()
                        .requestMatchers("/api/combos/**").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/districts/**", "/api/wards/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/orders").permitAll()
                        .requestMatchers("/api/payments/**").permitAll()

                        // Protected endpoints (yêu cầu JWT)
                        .requestMatchers("/api/orders/**").authenticated()
                        .requestMatchers("/api/users/**").authenticated()
                        .requestMatchers("/api/cart/**").authenticated()
                        .requestMatchers("/api/points/**").authenticated()
                        .requestMatchers("/api/favorites/**").authenticated() // Thêm bảo vệ cho favorites
                        .requestMatchers("/api/notifications/**").authenticated() // Bảo vệ cho notifications (tất cả endpoints)
                        .requestMatchers("/api/notifications/user/**").authenticated() // Notifications cho User
                        .requestMatchers("/api/notifications/staff/**").hasAnyRole("STAFF", "ADMIN") // Notifications cho Staff

                        // FEEDBACKS
                        .requestMatchers(HttpMethod.GET, "/api/feedback-media/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/feedback-media/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/feedback-media/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/feedback-media/**").hasRole("ADMIN")

                        // COUPONS
                        .requestMatchers(HttpMethod.GET, "/api/coupons/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/coupons/code/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/coupons").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/coupons/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/coupons/**").hasRole("ADMIN")

                        // Các request khác cần authentication
                        .anyRequest().authenticated()
                )
                // Thêm JWT filter vào chain
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Sử dụng allowedOriginPatterns thay vì setAllowedOriginPatterns cho consistency
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "https://oder-4c1f2.web.app",
                "http://localhost:5173",
                "https://dongxanhfoodorder.shop",
                "https://dongxanhfood.shop"
        ));

        // Cho phép tất cả HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Cho phép tất cả headers
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Cho phép credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Cache preflight response
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}

