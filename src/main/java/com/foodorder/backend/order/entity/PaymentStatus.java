package com.foodorder.backend.order.entity;

public enum PaymentStatus {
    PENDING,   // Chưa thanh toán
    PAID,      // Đã thanh toán
    FAILED,    // Lỗi giao dịch
    CANCELLED  // Đã huỷ thanh toán
}
