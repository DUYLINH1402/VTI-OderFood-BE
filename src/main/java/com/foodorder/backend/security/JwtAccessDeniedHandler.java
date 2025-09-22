package com.foodorder.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodorder.backend.exception.ApiError;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Xử lý các trường hợp người dùng đã đăng nhập nhưng không có quyền truy cập
 * (ví dụ: user thường truy cập endpoint chỉ dành cho admin)
 */
@Component
@Slf4j
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        log.warn("Access denied for user to: {} {}", request.getMethod(), request.getRequestURI());

        // Tạo response lỗi chuẩn hóa
        ApiError apiError = new ApiError(
                HttpServletResponse.SC_FORBIDDEN,
                "Bạn không có quyền truy cập tài nguyên này",
                "ACCESS_DENIED",
                null,
                "Token không hợp lệ hoặc đã hết hạn"
        );

        // Set response header và content type
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        // Trả về JSON response
        response.getWriter().write(objectMapper.writeValueAsString(apiError));
        response.getWriter().flush();
    }
}
