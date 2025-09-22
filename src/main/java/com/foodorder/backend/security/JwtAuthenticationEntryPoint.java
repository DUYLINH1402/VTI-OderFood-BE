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

/**
 * Xử lý các trường hợp người dùng truy cập endpoint cần authentication
 * mà không có token hợp lệ (chưa đăng nhập hoặc token hết hạn)
 */
@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {

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
