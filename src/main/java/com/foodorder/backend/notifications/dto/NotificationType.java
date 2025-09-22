package com.foodorder.backend.notifications.dto;

/**
 * Enum định nghĩa các loại thông báo trong hệ thống
 */
public enum NotificationType {
    ORDER_CONFIRMED("ORDER_CONFIRMED", "Đơn hàng đã được xác nhận"),
    ORDER_PREPARING("ORDER_PREPARING", "Đơn hàng đang được chuẩn bị"),
    ORDER_READY("ORDER_READY", "Đơn hàng đã sẵn sàng"),
    ORDER_DELIVERING("ORDER_DELIVERING", "Đơn hàng đang được giao"),
    ORDER_DELIVERED("ORDER_DELIVERED", "Đơn hàng đã được giao thành công"),
    ORDER_CANCELLED("ORDER_CANCELLED", "Đơn hàng đã bị hủy"),
    ORDER_REJECTED("ORDER_REJECTED", "Đơn hàng bị từ chối"),
    PAYMENT_SUCCESS("PAYMENT_SUCCESS", "Thanh toán thành công"),
    PAYMENT_FAILED("PAYMENT_FAILED", "Thanh toán thất bại"),
    PROMOTION("PROMOTION", "Khuyến mãi mới"),
    SYSTEM("SYSTEM", "Thông báo hệ thống");

    private final String code;
    private final String description;

    NotificationType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
