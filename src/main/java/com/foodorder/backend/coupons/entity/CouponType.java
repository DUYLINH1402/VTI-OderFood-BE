package com.foodorder.backend.coupons.entity;

/**
 * Enum loại coupon theo cách phân phối
 */
public enum CouponType {
    PUBLIC,      // Coupon công khai, ai cũng có thể dùng
    PRIVATE,     // Coupon riêng tư, chỉ user được chỉ định mới dùng được
    FIRST_ORDER, // Coupon dành cho đơn hàng đầu tiên
    BIRTHDAY,    // Coupon sinh nhật
    LOYALTY      // Coupon cho khách hàng thân thiết
}
