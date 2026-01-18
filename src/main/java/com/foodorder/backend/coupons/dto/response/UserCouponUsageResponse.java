package com.foodorder.backend.coupons.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO chứa thông tin sử dụng coupon của một User
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCouponUsageResponse {

    // === THÔNG TIN USER ===
    private Long userId;
    private String username;
    private String fullName;
    private String email;

    // === THỐNG KÊ TỔNG QUAN ===
    private Long totalCouponsUsed;        // Tổng số coupon đã sử dụng
    private Double totalDiscountReceived; // Tổng tiền đã được giảm
    private Double averageDiscountPerOrder; // Trung bình tiền giảm mỗi đơn

    // === LỊCH SỬ SỬ DỤNG ===
    private List<CouponUsageDetail> usageHistory;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CouponUsageDetail {
        private Long usageId;
        private Long couponId;
        private String couponCode;
        private String couponTitle;
        private Long orderId;
        private Double discountAmount;
        private LocalDateTime usedAt;
    }
}

