package com.foodorder.backend.order.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum định nghĩa các trạng thái đơn hàng
 * Workflow: PENDING -> PROCESSING -> CONFIRMED -> DELIVERING -> COMPLETED
 */
@Getter
@AllArgsConstructor
public enum OrderStatus {
    PENDING("PENDING", "Chờ thanh toán"),
    PROCESSING("PROCESSING", "Đã thanh toán, chờ xác nhận"),
    CONFIRMED("CONFIRMED", "Đã xác nhận, đang chế biến"),
    DELIVERING("DELIVERING", "Đang giao hàng"),
    COMPLETED("COMPLETED", "Hoàn thành"),
    CANCELLED("CANCELLED", "Đã hủy");

    private final String code;
    private final String description;
}
