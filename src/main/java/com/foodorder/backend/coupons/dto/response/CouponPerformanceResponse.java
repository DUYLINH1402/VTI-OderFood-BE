package com.foodorder.backend.coupons.dto.response;

import lombok.*;
import java.time.LocalDateTime;

/**
 * DTO chứa thông tin hiệu suất của một Coupon cụ thể
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponPerformanceResponse {

    // === THÔNG TIN CƠ BẢN ===
    private Long couponId;
    private String code;
    private String title;
    private String description;
    private String discountType;
    private Double discountValue;
    private String couponType;
    private String status;

    // === THỜI GIAN ===
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long daysActive;           // Số ngày đã hoạt động
    private Long daysRemaining;        // Số ngày còn lại

    // === THỐNG KÊ SỬ DỤNG ===
    private Integer maxUsage;          // Số lần sử dụng tối đa
    private Integer usedCount;         // Số lần đã sử dụng
    private Integer remainingUsage;    // Số lần còn lại
    private Double usageRate;          // Tỷ lệ sử dụng (%)

    // === HIỆU QUẢ ===
    private Double totalDiscountAmount;    // Tổng tiền đã giảm
    private Double averageDiscountAmount;  // Trung bình tiền giảm mỗi lần
    private Long uniqueUsersCount;         // Số user đã sử dụng
    private Double averageUsagePerDay;     // Trung bình lượt sử dụng/ngày

    // === ĐIỀU KIỆN ===
    private Double minOrderAmount;
    private Double maxDiscountAmount;
    private Integer maxUsagePerUser;
}

