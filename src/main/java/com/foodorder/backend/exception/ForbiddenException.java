package com.foodorder.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception cho trường hợp không có quyền truy cập
 * Ví dụ: Thao tác trên dữ liệu được bảo vệ (isProtected = true) mà không phải SUPER_ADMIN
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public class ForbiddenException extends RuntimeException {
    private final String errorCode;

    public ForbiddenException(String message) {
        super(message);
        this.errorCode = "FORBIDDEN";
    }

    public ForbiddenException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}

