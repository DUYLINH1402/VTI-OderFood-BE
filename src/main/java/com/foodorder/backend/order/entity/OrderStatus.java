package com.foodorder.backend.order.entity;
public enum OrderStatus {
    PENDING,         // Đơn mới tạo, chờ xử lý
    PROCESSING,      // Đang chuẩn bị/giao hàng
    COMPLETED,       // Đã giao thành công
    CANCELLED        // Đã huỷ
}

