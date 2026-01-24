package com.foodorder.backend.coupons.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Response chứa thống kê tổng quan về mã giảm giá")
public class CouponStatisticsResponse {

    // === TỔNG QUAN ===
    @Schema(description = "Tổng số coupon", example = "50")
    private Long totalCoupons;

    @Schema(description = "Số coupon đang hoạt động", example = "30")
    private Long activeCoupons;

    @Schema(description = "Số coupon đã hết hạn", example = "15")
    private Long expiredCoupons;

    @Schema(description = "Số coupon bị vô hiệu hóa", example = "5")
    private Long inactiveCoupons;

    @Schema(description = "Số coupon đã hết lượt sử dụng", example = "10")
    private Long usedOutCoupons;

    // === THỐNG KÊ SỬ DỤNG ===
    @Schema(description = "Tổng lượt sử dụng coupon", example = "1500")
    private Long totalUsageCount;

    @Schema(description = "Tổng số tiền giảm giá (VND)", example = "15000000")
    private Double totalDiscountAmount;

    @Schema(description = "Số tiền giảm giá trung bình mỗi lần (VND)", example = "10000")
    private Double averageDiscountAmount;

    // === PHÂN BỔ THEO LOẠI ===
    @Schema(description = "Số lượng theo loại coupon", example = "{\"PUBLIC\": 25, \"PRIVATE\": 15, \"FIRST_ORDER\": 10}")
    private Map<String, Long> couponsByType;

    @Schema(description = "Số lượng theo trạng thái", example = "{\"ACTIVE\": 30, \"INACTIVE\": 5, \"EXPIRED\": 15}")
    private Map<String, Long> couponsByStatus;

    @Schema(description = "Số lượng theo loại giảm giá", example = "{\"PERCENT\": 35, \"FIXED\": 15}")
    private Map<String, Long> couponsByDiscountType;

    // === HIỆU SUẤT ===
    @Schema(description = "Tỷ lệ sử dụng (%)", example = "65.5")
    private Double usageRate;

    @Schema(description = "Tỷ lệ coupon đang hoạt động (%)", example = "60.0")
    private Double activeRate;
}

