package com.foodorder.backend.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiError {
    private int status;
    private String message;
    private String errorCode;
    private Object errors; // Có thể là Map (field error), hoặc null
    private String details; // Thêm field details cho chatbot
}
