package com.foodorder.backend.coupons.dto.request;

import com.foodorder.backend.coupons.entity.DiscountType;
import com.foodorder.backend.coupons.entity.CouponType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho request tạo/sửa coupon
 * Chỉ chứa các trường cần thiết khi client gửi lên để tạo hoặc cập nhật coupon
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body để tạo/cập nhật mã giảm giá (coupon)")
public class CouponRequest {

    @Schema(
        description = "Mã coupon (3-20 ký tự, không trùng)",
        example = "SUMMER2025",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Coupon code must not be blank")
    @Size(min = 3, max = 20, message = "Coupon code must be between 3 and 20 characters")
    private String code;

    @Schema(description = "Mô tả chi tiết coupon", example = "Giảm giá mùa hè 2025, áp dụng cho tất cả món ăn")
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Schema(description = "Tiêu đề coupon", example = "Khuyến mãi mùa hè")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    @Schema(
        description = "Loại giảm giá",
        example = "PERCENT",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = {"PERCENT", "FIXED"}
    )
    @NotNull(message = "Discount type must not be null")
    private DiscountType discountType;

    @Schema(
        description = "Giá trị giảm (% hoặc số tiền tùy loại). Với PERCENT tối đa 100",
        example = "20",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Discount value must not be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Discount value must be greater than 0")
    @DecimalMax(value = "100.0", message = "Discount percentage must not exceed 100%")
    private Double discountValue;

    // === ĐIỀU KIỆN ÁP DỤNG NÂNG CAO ===
    @Schema(description = "Giá trị đơn hàng tối thiểu để áp dụng coupon", example = "100000")
    @DecimalMin(value = "0.0", message = "Min order amount must be non-negative")
    private Double minOrderAmount;

    @Schema(description = "Số tiền giảm tối đa (bắt buộc với PERCENT)", example = "50000")
    @DecimalMin(value = "0.0", message = "Max discount amount must be non-negative")
    private Double maxDiscountAmount;

    @Schema(description = "Số lần sử dụng tối đa mỗi user", example = "1")
    @Min(value = 1, message = "Max usage per user must be at least 1")
    private Integer maxUsagePerUser;

    @Schema(
        description = "Thời gian bắt đầu hiệu lực",
        example = "2025-02-01T00:00:00",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Start date must not be null")
    @Future(message = "Start date must be in the future")
    private LocalDateTime startDate;

    @Schema(
        description = "Thời gian hết hạn (phải sau startDate)",
        example = "2025-03-01T23:59:59",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "End date must not be null")
    private LocalDateTime endDate;

    @Schema(
        description = "Tổng số lần sử dụng tối đa",
        example = "100",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Max usage must not be null")
    @Min(value = 1, message = "Max usage must be at least 1")
    private Integer maxUsage;

    @Schema(
        description = "Loại coupon: PUBLIC (công khai), PRIVATE (riêng tư cho user cụ thể)",
        example = "PUBLIC",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = {"PUBLIC", "PRIVATE"}
    )
    @NotNull(message = "Coupon type must not be null")
    private CouponType couponType;

    // === QUAN HỆ VỚI CÁC ENTITY KHÁC ===
    @Schema(description = "Danh sách ID danh mục được áp dụng (để trống = tất cả)", example = "[1, 2]")
    private List<Long> applicableCategoryIds;

    @Schema(description = "Danh sách ID món ăn được áp dụng (để trống = tất cả)", example = "[1, 2, 3]")
    private List<Long> applicableFoodIds;

    @Schema(description = "Danh sách ID user được áp dụng (bắt buộc với PRIVATE)", example = "[1, 2]")
    private List<Long> applicableUserIds;

    // === VALIDATION CUSTOM ===
    @AssertTrue(message = "End date must be after start date")
    @Schema(hidden = true)
    private boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) {
            return true;
        }
        return endDate.isAfter(startDate);
    }

    @AssertTrue(message = "Max discount amount is required for percentage discount")
    @Schema(hidden = true)
    private boolean isMaxDiscountAmountValidForPercent() {
        if (discountType == DiscountType.PERCENT) {
            return maxDiscountAmount != null && maxDiscountAmount > 0;
        }
        return true;
    }

    @AssertTrue(message = "Applicable user IDs are required for private coupons")
    @Schema(hidden = true)
    private boolean isApplicableUsersValidForPrivate() {
        if (couponType == CouponType.PRIVATE) {
            return applicableUserIds != null && !applicableUserIds.isEmpty();
        }
        return true;
    }
}
