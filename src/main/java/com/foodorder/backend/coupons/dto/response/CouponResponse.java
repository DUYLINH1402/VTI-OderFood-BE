package com.foodorder.backend.coupons.dto.response;

import com.foodorder.backend.coupons.entity.CouponStatus;
import com.foodorder.backend.coupons.entity.CouponType;
import com.foodorder.backend.coupons.entity.DiscountType;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho response trả về thông tin coupon
 * Chứa đầy đủ thông tin coupon để trả về cho client
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponResponse {
    private Long id;
    private String code;
    private String title;
    private String description;
    private DiscountType discountType;
    private Double discountValue;

    // === ĐIỀU KIỆN ÁP DỤNG ===
    private Double minOrderAmount;
    private Double maxDiscountAmount;
    private Integer maxUsagePerUser;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer maxUsage;
    private Integer usedCount;
    private CouponStatus status;
    private CouponType couponType;

    // === THÔNG TIN BỔ SUNG ===
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // === DANH SÁCH ÁP DỤNG ===
    private List<String> applicableCategoryNames;
    private List<String> applicableFoodNames;
    private Integer applicableUserCount; // Số lượng user được áp dụng (không trả về danh sách để bảo mật)

    // === TRẠNG THÁI COMPUTED ===
    private Boolean isValid; // Có còn hiệu lực không
    private Boolean isExpiringSoon; // Sắp hết hạn (trong 7 ngày)
    private Integer remainingUsage; // Số lần còn lại có thể sử dụng
    private Double usagePercentage; // Phần trăm đã sử dụng

    // Constructor từ Entity (helper method sẽ tính toán các trường computed)
    public static CouponResponse fromEntity(com.foodorder.backend.coupons.entity.Coupon coupon) {
        LocalDateTime now = LocalDateTime.now();
        Integer remaining = coupon.getMaxUsage() - coupon.getUsedCount();
        Double usagePercent = (double) coupon.getUsedCount() / coupon.getMaxUsage() * 100;
        Boolean expiringSoon = coupon.getEndDate().isBefore(now.plusDays(7));

        return CouponResponse.builder()
            .id(coupon.getId())
            .code(coupon.getCode())
            .title(coupon.getTitle())
            .description(coupon.getDescription())
            .discountType(coupon.getDiscountType())
            .discountValue(coupon.getDiscountValue())
            .minOrderAmount(coupon.getMinOrderAmount())
            .maxDiscountAmount(coupon.getMaxDiscountAmount())
            .maxUsagePerUser(coupon.getMaxUsagePerUser())
            .startDate(coupon.getStartDate())
            .endDate(coupon.getEndDate())
            .maxUsage(coupon.getMaxUsage())
            .usedCount(coupon.getUsedCount())
            .status(coupon.getStatus())
            .couponType(coupon.getCouponType())
            .createdAt(coupon.getCreatedAt())
            .updatedAt(coupon.getUpdatedAt())
            .isValid(coupon.isValid())
            .isExpiringSoon(expiringSoon)
            .remainingUsage(remaining)
            .usagePercentage(usagePercent)
            .applicableUserCount(coupon.getApplicableUsers() != null ? coupon.getApplicableUsers().size() : 0)
            .build();
    }
}
