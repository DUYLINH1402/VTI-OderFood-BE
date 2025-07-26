// CUSTOM EXCEPTION ĐỂ BÁO LỖI KHÔNG TÌM THẤY DỮ LIỆU

package com.foodorder.backend.exception;

public class ResourceNotFoundException extends RuntimeException {
    private final String errorCode;

    public ResourceNotFoundException(String message) {
        super(message);
        this.errorCode = "NOT_FOUND";
    }

    public ResourceNotFoundException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
