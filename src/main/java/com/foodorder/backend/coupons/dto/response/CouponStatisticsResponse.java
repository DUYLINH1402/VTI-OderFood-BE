package com.foodorder.backend.coupons.dto.response;

import lombok.*;
import java.util.List;
import java.util.Map;

/**
 * DTO chứa thống kê tổng quan về Coupon
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponStatisticsResponse {

    // === TỔNG QUAN ===
    private Long totalCoupons;           // Tổng số coupon
    private Long activeCoupons;          // Số coupon đang hoạt động
    private Long expiredCoupons;         // Số coupon đã hết hạn
    private Long inactiveCoupons;        // Số coupon bị vô hiệu hóa
    private Long usedOutCoupons;         // Số coupon đã hết lượt sử dụng

    // === THỐNG KÊ SỬ DỤNG ===
    private Long totalUsageCount;        // Tổng lượt sử dụng coupon
    private Double totalDiscountAmount;  // Tổng số tiền giảm giá
    private Double averageDiscountAmount; // Số tiền giảm giá trung bình mỗi lần

    // === PHÂN BỔ THEO LOẠI ===
    private Map<String, Long> couponsByType;     // Số lượng theo loại (PUBLIC, PRIVATE, FIRST_ORDER)
    private Map<String, Long> couponsByStatus;   // Số lượng theo trạng thái
    private Map<String, Long> couponsByDiscountType; // Số lượng theo loại giảm giá (PERCENT, AMOUNT)

    // === HIỆU SUẤT ===
    private Double usageRate;            // Tỷ lệ sử dụng (%)
    private Double activeRate;           // Tỷ lệ coupon đang hoạt động (%)
}

