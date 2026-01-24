package com.foodorder.backend.config;

import com.foodorder.backend.security.JwtAccessDeniedHandler;
import com.foodorder.backend.security.JwtAuthenticationEntryPoint;
import com.foodorder.backend.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
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

    /**
     * SecurityFilterChain riêng cho Swagger - KHÔNG áp dụng bất kỳ security nào
     * Order = 1 để được xử lý TRƯỚC filterChain chính
     */
    @Bean
    @Order(1)
    public SecurityFilterChain swaggerSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher(
                        "/swagger-ui/**",       // Quan trọng: Phải có /**
                        "/v3/api-docs/**",      // Khớp với springdoc.api-docs.path
                        "/v3/api-docs",
                        "/swagger-resources/**",
                        "/webjars/**"
                )
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Cấu hình xử lý lỗi authentication và authorization
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint) // Xử lý khi chưa đăng nhập
                        .accessDeniedHandler(jwtAccessDeniedHandler) // Xử lý khi không có quyền
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/app/**").permitAll()     // STOMP destination prefix
                        .requestMatchers("/topic/**").permitAll()   // Message broker topics
                        .requestMatchers("/queue/**").permitAll()
                        .requestMatchers("/ws/staff-orders/**").permitAll()

                        // Chat API endpoints - Phân quyền chi tiết
                        .requestMatchers(HttpMethod.GET, "/api/chat/history").hasRole("USER")
                        .requestMatchers(HttpMethod.GET, "/api/chat/unread").hasRole("USER")
                        .requestMatchers(HttpMethod.PUT, "/api/chat/mark-read/**").hasAnyRole("USER", "STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/chat/staff/all-messages").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/chat/staff/user/*/messages").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/chat/staff/users").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/chat/staff/unread-count").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/chat/admin/statistics").hasRole("ADMIN")

                        // Chatbot AI
                        .requestMatchers("/api/chatbot/**").permitAll()

                        // Cho phép static resources và test pages
                        .requestMatchers("/static/**").permitAll()


                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        
                        // FOODS - Phân quyền chi tiết
                        .requestMatchers(HttpMethod.GET, "/api/foods/management").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/foods/*/status").hasAnyRole("STAFF", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/foods").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/foods/upload").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/foods/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/foods/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/foods/**").permitAll() // Public GET cho các API còn lại
                        
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

                        // LIKES - Toggle like yêu cầu đăng nhập, xem thông tin like cho phép public
                        .requestMatchers(HttpMethod.POST, "/api/likes/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/likes/check/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/likes/**").permitAll()

                        // SHARES - Ghi nhận share cho phép cả khách vãng lai, xem số share public
                        .requestMatchers("/api/shares/**").permitAll()

                        // COMMENTS - Phân quyền chi tiết cho User và Admin
                        .requestMatchers(HttpMethod.GET, "/api/comments/**").permitAll() // Xem comment public
                        .requestMatchers(HttpMethod.POST, "/api/comments").authenticated() // Tạo comment yêu cầu đăng nhập
                        .requestMatchers(HttpMethod.PUT, "/api/comments/**").authenticated() // Sửa comment yêu cầu đăng nhập
                        .requestMatchers(HttpMethod.DELETE, "/api/comments/**").authenticated() // Xóa comment yêu cầu đăng nhập
                        .requestMatchers("/api/admin/comments/**").hasRole("ADMIN") // Admin quản lý comment

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
