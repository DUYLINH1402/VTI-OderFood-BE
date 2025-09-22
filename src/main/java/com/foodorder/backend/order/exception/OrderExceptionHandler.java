package com.foodorder.backend.order.exception;

import com.foodorder.backend.exception.BadRequestException;
import com.foodorder.backend.order.dto.response.OrderErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.foodorder.backend.order")
public class OrderExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<OrderErrorResponse> handleBadRequestException(BadRequestException e) {
        OrderErrorResponse errorResponse = OrderErrorResponse.builder()
                .success(false)
                .message(e.getMessage())
                .errorCode(e.getErrorCode())
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<OrderErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        OrderErrorResponse errorResponse = OrderErrorResponse.builder()
                .success(false)
                .message(e.getMessage())
                .errorCode("INVALID_REQUEST")
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<OrderErrorResponse> handleGeneralException(Exception e) {
        OrderErrorResponse errorResponse = OrderErrorResponse.builder()
                .success(false)
                .message("Có lỗi xảy ra trong quá trình xử lý đơn hàng")
                .errorCode("INTERNAL_ERROR")
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
