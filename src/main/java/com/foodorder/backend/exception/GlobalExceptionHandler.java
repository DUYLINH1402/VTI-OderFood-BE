package com.foodorder.backend.exception;

import com.foodorder.backend.security.exception.JwtTokenExpiredException;
import com.foodorder.backend.security.exception.JwtTokenInvalidException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.validation.FieldError;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Xử lý custom exception với errorCode (ví dụ BadRequestException...)
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException ex) {
        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                ex.getErrorCode(),
                null);
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        ApiError apiError = new ApiError(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                ex.getErrorCode(),
                null);
        return new ResponseEntity<>(apiError, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TooManyRequestException.class)
    public ResponseEntity<ApiError> handleTooManyRequest(TooManyRequestException ex) {
        ApiError apiError = new ApiError(
                429,
                ex.getMessage(),
                ex.getErrorCode(),
                null);
        return new ResponseEntity<>(apiError, HttpStatus.TOO_MANY_REQUESTS);
    }

    // Xử lý JWT token expired
    @ExceptionHandler(JwtTokenExpiredException.class)
    public ResponseEntity<ApiError> handleJwtTokenExpired(JwtTokenExpiredException ex) {
        ApiError apiError = new ApiError(
                HttpStatus.UNAUTHORIZED.value(),
                "JWT token has expired",
                "JWT_TOKEN_EXPIRED",
                null);
        return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
    }

    // Xử lý JWT token invalid
    @ExceptionHandler(JwtTokenInvalidException.class)
    public ResponseEntity<ApiError> handleJwtTokenInvalid(JwtTokenInvalidException ex) {
        ApiError apiError = new ApiError(
                HttpStatus.UNAUTHORIZED.value(),
                "JWT token is invalid",
                "JWT_TOKEN_INVALID",
                null);
        return new ResponseEntity<>(apiError, HttpStatus.UNAUTHORIZED);
    }

    // Xử lý lỗi validate @Valid (ví dụ khi POST/PUT dữ liệu bị thiếu, sai kiểu)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                "VALIDATION_ERROR",
                errors);
        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    // Xử lý tất cả các exception chưa bắt riêng
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex) {
        ApiError apiError = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error",
                "INTERNAL_ERROR",
                null);
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
