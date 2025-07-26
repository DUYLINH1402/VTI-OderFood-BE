package com.foodorder.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
public class TooManyRequestException extends RuntimeException {
    private final String errorCode;

    public TooManyRequestException(String message) {
        super(message);
        this.errorCode = "TOO_MANY_REQUESTS";
    }

    public TooManyRequestException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
