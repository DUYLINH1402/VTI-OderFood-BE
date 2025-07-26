package com.foodorder.backend.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@AllArgsConstructor
public class ApiError {
    private int status;
    private String message;
    private String errorCode;
    private Object errors; // Có thể là Map (field error), hoặc null


}

