package com.foodorder.backend.coupons.entity;

/**
 * Enum trạng thái của coupon
 */
public enum CouponStatus {
    ACTIVE,     // Đang hoạt động
    EXPIRED,    // Đã hết hạn
    INACTIVE,   // Tạm ngừng hoạt động (do admin)
    USED_OUT,   // Đã hết lượt sử dụng
    DRAFT       // Nháp (chưa được kích hoạt)
}
