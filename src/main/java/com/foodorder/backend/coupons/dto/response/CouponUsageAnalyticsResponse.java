package com.foodorder.backend.coupons.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO chứa phân tích chi tiết về việc sử dụng Coupon
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponUsageAnalyticsResponse {

    // === THÔNG TIN THỜI GIAN ===
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // === THỐNG KÊ TRONG KHOẢNG THỜI GIAN ===
    private Long usageCount;              // Số lần sử dụng trong khoảng thời gian
    private Double totalDiscountAmount;   // Tổng tiền giảm
    private Double averageDiscountPerUsage; // Trung bình tiền giảm mỗi lần
    private Long uniqueUsersCount;        // Số user đã sử dụng

    // === XU HƯỚNG THEO NGÀY ===
    private List<DailyUsageData> dailyUsageData;

    // === TOP COUPON ===
    private List<TopCouponData> topCouponsByUsage;    // Top coupon theo lượt sử dụng
    private List<TopCouponData> topCouponsByDiscount; // Top coupon theo tổng tiền giảm

    // === PHÂN TÍCH THEO LOẠI ===
    private Map<String, UsageByTypeData> usageByType;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyUsageData {
        private String date;
        private Long usageCount;
        private Double discountAmount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TopCouponData {
        private Long couponId;
        private String couponCode;
        private String title;
        private Long usageCount;
        private Double totalDiscountAmount;
        private String discountType;
        private Double discountValue;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UsageByTypeData {
        private String type;
        private Long usageCount;
        private Double totalDiscountAmount;
        private Double percentage;
    }
}

