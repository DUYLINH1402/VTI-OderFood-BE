package com.foodorder.backend.coupons.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Response chứa phân tích chi tiết về việc sử dụng mã giảm giá")
public class CouponUsageAnalyticsResponse {

    // === THÔNG TIN THỜI GIAN ===
    @Schema(description = "Thời gian bắt đầu phân tích", example = "2025-01-01T00:00:00")
    private LocalDateTime startDate;

    @Schema(description = "Thời gian kết thúc phân tích", example = "2025-01-31T23:59:59")
    private LocalDateTime endDate;

    // === THỐNG KÊ TRONG KHOẢNG THỜI GIAN ===
    @Schema(description = "Số lần sử dụng trong khoảng thời gian", example = "500")
    private Long usageCount;

    @Schema(description = "Tổng tiền giảm (VND)", example = "5000000")
    private Double totalDiscountAmount;

    @Schema(description = "Trung bình tiền giảm mỗi lần (VND)", example = "10000")
    private Double averageDiscountPerUsage;

    @Schema(description = "Số user đã sử dụng", example = "200")
    private Long uniqueUsersCount;

    // === XU HƯỚNG THEO NGÀY ===
    @Schema(description = "Dữ liệu sử dụng theo ngày")
    private List<DailyUsageData> dailyUsageData;

    // === TOP COUPON ===
    @Schema(description = "Top coupon theo lượt sử dụng")
    private List<TopCouponData> topCouponsByUsage;

    @Schema(description = "Top coupon theo tổng tiền giảm")
    private List<TopCouponData> topCouponsByDiscount;

    // === PHÂN TÍCH THEO LOẠI ===
    @Schema(description = "Phân tích sử dụng theo loại coupon")
    private Map<String, UsageByTypeData> usageByType;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Dữ liệu sử dụng coupon theo ngày")
    public static class DailyUsageData {
        @Schema(description = "Ngày", example = "2025-01-15")
        private String date;

        @Schema(description = "Số lượt sử dụng", example = "50")
        private Long usageCount;

        @Schema(description = "Tổng tiền giảm (VND)", example = "500000")
        private Double discountAmount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Thông tin top coupon")
    public static class TopCouponData {
        @Schema(description = "ID coupon", example = "1")
        private Long couponId;

        @Schema(description = "Mã coupon", example = "SUMMER2025")
        private String couponCode;

        @Schema(description = "Tiêu đề", example = "Khuyến mãi mùa hè")
        private String title;

        @Schema(description = "Số lượt sử dụng", example = "100")
        private Long usageCount;

        @Schema(description = "Tổng tiền giảm (VND)", example = "1000000")
        private Double totalDiscountAmount;

        @Schema(description = "Loại giảm giá", example = "PERCENT")
        private String discountType;

        @Schema(description = "Giá trị giảm", example = "20")
        private Double discountValue;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Phân tích sử dụng theo loại coupon")
    public static class UsageByTypeData {
        @Schema(description = "Loại coupon", example = "PUBLIC")
        private String type;

        @Schema(description = "Số lượt sử dụng", example = "300")
        private Long usageCount;

        @Schema(description = "Tổng tiền giảm (VND)", example = "3000000")
        private Double totalDiscountAmount;

        @Schema(description = "Phần trăm so với tổng", example = "60.0")
        private Double percentage;
    }
}

