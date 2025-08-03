package com.foodorder.backend.coupons.dto.request;

import com.foodorder.backend.coupons.entity.DiscountType;
import com.foodorder.backend.coupons.entity.CouponType;
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
public class CouponRequest {
    @NotBlank(message = "Coupon code must not be blank")
    @Size(min = 3, max = 20, message = "Coupon code must be between 3 and 20 characters")
    private String code;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;

    @NotNull(message = "Discount type must not be null")
    private DiscountType discountType;

    @NotNull(message = "Discount value must not be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Discount value must be greater than 0")
    @DecimalMax(value = "100.0", message = "Discount percentage must not exceed 100%")
    private Double discountValue;

    // === ĐIỀU KIỆN ÁP DỤNG NÂNG CAO ===
    @DecimalMin(value = "0.0", message = "Min order amount must be non-negative")
    private Double minOrderAmount;

    @DecimalMin(value = "0.0", message = "Max discount amount must be non-negative")
    private Double maxDiscountAmount;

    @Min(value = 1, message = "Max usage per user must be at least 1")
    private Integer maxUsagePerUser;

    @NotNull(message = "Start date must not be null")
    @Future(message = "Start date must be in the future")
    private LocalDateTime startDate;

    @NotNull(message = "End date must not be null")
    private LocalDateTime endDate;

    @NotNull(message = "Max usage must not be null")
    @Min(value = 1, message = "Max usage must be at least 1")
    private Integer maxUsage;

    @NotNull(message = "Coupon type must not be null")
    private CouponType couponType;

    // === QUAN HỆ VỚI CÁC ENTITY KHÁC ===
    private List<Long> applicableCategoryIds;
    private List<Long> applicableFoodIds;
    private List<Long> applicableUserIds; // Chỉ dành cho PRIVATE coupon

    // === VALIDATION CUSTOM ===
    @AssertTrue(message = "End date must be after start date")
    private boolean isEndDateAfterStartDate() {
        if (startDate == null || endDate == null) {
            return true; // Let @NotNull handle null validation
        }
        return endDate.isAfter(startDate);
    }

    @AssertTrue(message = "Max discount amount is required for percentage discount")
    private boolean isMaxDiscountAmountValidForPercent() {
        if (discountType == DiscountType.PERCENT) {
            return maxDiscountAmount != null && maxDiscountAmount > 0;
        }
        return true;
    }

    @AssertTrue(message = "Applicable user IDs are required for private coupons")
    private boolean isApplicableUsersValidForPrivate() {
        if (couponType == CouponType.PRIVATE) {
            return applicableUserIds != null && !applicableUserIds.isEmpty();
        }
        return true;
    }
}
