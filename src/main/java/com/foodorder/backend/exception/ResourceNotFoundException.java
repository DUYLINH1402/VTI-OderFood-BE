// CUSTOM EXCEPTION ĐỂ BÁO LỖI KHÔNG TÌM THẤY DỮ LIỆU

package com.foodorder.backend.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
