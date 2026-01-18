package com.foodorder.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodorder.backend.exception.ApiError;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Xử lý các trường hợp người dùng truy cập endpoint cần authentication
 * mà không có token hợp lệ (chưa đăng nhập hoặc token hết hạn)
 */
@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Danh sách các URL patterns công khai (Swagger, API docs...)
     */
    private static final List<String> PUBLIC_URL_PREFIXES = Arrays.asList(
            "/swagger-ui",
            "/v3/api-docs",
            "/swagger-resources",
            "/webjars",
            "/configuration"
    );

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

        String path = request.getRequestURI();

        // Nếu là URL công khai (Swagger...), không trả về lỗi 401
        // Điều này không nên xảy ra nếu cấu hình đúng, nhưng để đảm bảo an toàn
        boolean isPublicUrl = PUBLIC_URL_PREFIXES.stream().anyMatch(path::startsWith);
        if (isPublicUrl) {
            log.debug("Public URL accessed without auth: {}", path);
            // Cho phép tiếp tục, không block
            return;
        }

        // Tạo response lỗi chuẩn hóa
        ApiError apiError = ApiError.builder()
                .status(HttpServletResponse.SC_UNAUTHORIZED)
                .message("Bạn cần đăng nhập để truy cập tài nguyên này")
                .errorCode("AUTHENTICATION_REQUIRED")
                .errors(null)
                .details("Token không hợp lệ hoặc đã hết hạn")
                .build();

        // Set response header và content type
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        // Trả về JSON response
        response.getWriter().write(objectMapper.writeValueAsString(apiError));
        response.getWriter().flush();
    }
}
