package com.foodorder.backend.coupons.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Response chứa thông tin hiệu suất của mã giảm giá")
public class CouponPerformanceResponse {

    // === THÔNG TIN CƠ BẢN ===
    @Schema(description = "ID của coupon", example = "1")
    private Long couponId;

    @Schema(description = "Mã coupon", example = "SUMMER2025")
    private String code;

    @Schema(description = "Tiêu đề coupon", example = "Khuyến mãi mùa hè")
    private String title;

    @Schema(description = "Mô tả coupon", example = "Giảm 20% cho tất cả món ăn")
    private String description;

    @Schema(description = "Loại giảm giá", example = "PERCENT")
    private String discountType;

    @Schema(description = "Giá trị giảm", example = "20")
    private Double discountValue;

    @Schema(description = "Loại coupon", example = "PUBLIC")
    private String couponType;

    @Schema(description = "Trạng thái coupon", example = "ACTIVE")
    private String status;

    // === THỜI GIAN ===
    @Schema(description = "Thời gian bắt đầu", example = "2025-02-01T00:00:00")
    private LocalDateTime startDate;

    @Schema(description = "Thời gian kết thúc", example = "2025-03-01T23:59:59")
    private LocalDateTime endDate;

    @Schema(description = "Số ngày đã hoạt động", example = "15")
    private Long daysActive;

    @Schema(description = "Số ngày còn lại", example = "10")
    private Long daysRemaining;

    // === THỐNG KÊ SỬ DỤNG ===
    @Schema(description = "Số lần sử dụng tối đa", example = "100")
    private Integer maxUsage;

    @Schema(description = "Số lần đã sử dụng", example = "45")
    private Integer usedCount;

    @Schema(description = "Số lần còn lại", example = "55")
    private Integer remainingUsage;

    @Schema(description = "Tỷ lệ sử dụng (%)", example = "45.0")
    private Double usageRate;

    // === HIỆU QUẢ ===
    @Schema(description = "Tổng tiền đã giảm (VND)", example = "1500000")
    private Double totalDiscountAmount;

    @Schema(description = "Trung bình tiền giảm mỗi lần (VND)", example = "33333")
    private Double averageDiscountAmount;

    @Schema(description = "Số user đã sử dụng", example = "30")
    private Long uniqueUsersCount;

    @Schema(description = "Trung bình lượt sử dụng/ngày", example = "3.0")
    private Double averageUsagePerDay;

    // === ĐIỀU KIỆN ===
    @Schema(description = "Giá trị đơn hàng tối thiểu", example = "100000")
    private Double minOrderAmount;

    @Schema(description = "Số tiền giảm tối đa", example = "50000")
    private Double maxDiscountAmount;

    @Schema(description = "Số lần sử dụng tối đa mỗi user", example = "1")
    private Integer maxUsagePerUser;
}

