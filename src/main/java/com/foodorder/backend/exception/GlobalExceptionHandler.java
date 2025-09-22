package com.foodorder.backend.exception;

import com.foodorder.backend.security.exception.JwtTokenExpiredException;
import com.foodorder.backend.security.exception.JwtTokenInvalidException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.FieldError;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Xử lý custom exception với errorCode (ví dụ BadRequestException...)
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex) {
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode())
                .errors(null)
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode())
                .errors(null)
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TooManyRequestException.class)
    public ResponseEntity<ApiError> handleTooManyRequest(TooManyRequestException ex) {
        ApiError apiError = ApiError.builder()
                .status(429)
                .message(ex.getMessage())
                .errorCode(ex.getErrorCode())
                .errors(null)
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.TOO_MANY_REQUESTS);
    }

    // Xử lý JWT token expired
    @ExceptionHandler(JwtTokenExpiredException.class)
    public ResponseEntity<ApiError> handleJwtTokenExpired(JwtTokenExpiredException ex) {
        log.warn("JWT token expired: {}", ex.getMessage());
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("Token đã hết hạn, vui lòng đăng nhập lại")
                .errorCode("JWT_TOKEN_EXPIRED")
                .errors(null)
                .details(ex.getMessage())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
    }

    // Xử lý JWT token invalid
    @ExceptionHandler(JwtTokenInvalidException.class)
    public ResponseEntity<ApiError> handleJwtTokenInvalid(JwtTokenInvalidException ex) {
        log.warn("Invalid JWT token: {}", ex.getMessage());
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("Token không hợp lệ, vui lòng đăng nhập lại")
                .errorCode("JWT_TOKEN_INVALID")
                .errors(null)
                .details(ex.getMessage())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
    }

    // Xử lý lỗi xác thực Spring Security
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("Xác thực thất bại")
                .errorCode("AUTHENTICATION_FAILED")
                .errors(null)
                .details(ex.getMessage())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
    }

    // Xử lý lỗi thông tin đăng nhập sai
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Bad credentials: {}", ex.getMessage());
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("Tên đăng nhập hoặc mật khẩu không đúng")
                .errorCode("INVALID_CREDENTIALS")
                .errors(null)
                .details("Vui lòng kiểm tra lại thông tin đăng nhập")
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
    }

    // Xử lý lỗi không có quyền truy cập
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.FORBIDDEN.value())
                .message("Bạn không có quyền truy cập tài nguyên này")
                .errorCode("ACCESS_DENIED")
                .errors(null)
                .details(ex.getMessage())
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.FORBIDDEN);
    }

    // Xử lý lỗi validate @Valid (ví dụ khi POST/PUT dữ liệu bị thiếu, sai kiểu)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Dữ liệu không hợp lệ")
                .errorCode("VALIDATION_ERROR")
                .errors(errors)
                .details("Vui lòng kiểm tra lại các trường dữ liệu")
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    // Xử lý tất cả các exception chưa bắt riêng - quan trọng để tránh crash server
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex) {
        // Log lỗi chi tiết để debug nhưng không trả về cho client (bảo mật)
        log.error("Unexpected error occurred", ex);
        ApiError apiError = ApiError.builder()
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Có lỗi xảy ra, vui lòng thử lại sau")
                .errorCode("INTERNAL_ERROR")
                .errors(null)
                .details("Lỗi hệ thống không xác định")
                .build();
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
