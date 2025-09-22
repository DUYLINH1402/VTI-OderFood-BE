package com.foodorder.backend.order.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderErrorResponse {
    private boolean success;
    private String message;
    private String errorCode;
    private long timestamp;

    // Các error codes phổ biến cho Order
    public static final String INSUFFICIENT_POINTS = "INSUFFICIENT_POINTS";
    public static final String POINTS_EXCEED_ORDER_VALUE = "POINTS_EXCEED_ORDER_VALUE";
    public static final String POINTS_GUEST_ORDER = "POINTS_GUEST_ORDER";
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String COUPON_INVALID = "COUPON_INVALID";
    public static final String FOOD_NOT_FOUND = "FOOD_NOT_FOUND";
    public static final String INVALID_REQUEST = "INVALID_REQUEST";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
}
