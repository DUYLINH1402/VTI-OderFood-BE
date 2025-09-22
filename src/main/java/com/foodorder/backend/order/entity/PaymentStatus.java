package com.foodorder.backend.order.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum định nghĩa các trạng thái thanh toán
 */
@Getter
@AllArgsConstructor
public enum PaymentStatus {
    PENDING("PENDING", "Chờ thanh toán"),
    PAID("PAID", "Đã thanh toán"),
    FAILED("FAILED", "Thanh toán thất bại"),
    REFUNDED("REFUNDED", "Đã hoàn tiền");

    private final String code;
    private final String description;
}
