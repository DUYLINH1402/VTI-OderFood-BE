package com.foodorder.backend.coupons.dto.response;

import com.foodorder.backend.coupons.entity.CouponStatus;
import com.foodorder.backend.coupons.entity.CouponType;
import com.foodorder.backend.coupons.entity.DiscountType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
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
@Schema(description = "Response chứa thông tin chi tiết mã giảm giá")
public class CouponResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID của coupon", example = "1")
    private Long id;

    @Schema(description = "Mã coupon", example = "SUMMER2025")
    private String code;

    @Schema(description = "Tiêu đề coupon", example = "Khuyến mãi mùa hè")
    private String title;

    @Schema(description = "Mô tả chi tiết coupon", example = "Giảm giá 20% cho tất cả món ăn")
    private String description;

    @Schema(description = "Loại giảm giá", example = "PERCENT", allowableValues = {"PERCENT", "FIXED"})
    private DiscountType discountType;

    @Schema(description = "Giá trị giảm (% hoặc số tiền)", example = "20")
    private Double discountValue;

    // === ĐIỀU KIỆN ÁP DỤNG ===
    @Schema(description = "Giá trị đơn hàng tối thiểu", example = "100000")
    private Double minOrderAmount;

    @Schema(description = "Số tiền giảm tối đa", example = "50000")
    private Double maxDiscountAmount;

    @Schema(description = "Số lần sử dụng tối đa mỗi user", example = "1")
    private Integer maxUsagePerUser;

    @Schema(description = "Thời gian bắt đầu hiệu lực", example = "2025-02-01T00:00:00")
    private LocalDateTime startDate;

    @Schema(description = "Thời gian hết hạn", example = "2025-03-01T23:59:59")
    private LocalDateTime endDate;

    @Schema(description = "Tổng số lần sử dụng tối đa", example = "100")
    private Integer maxUsage;

    @Schema(description = "Số lần đã sử dụng", example = "45")
    private Integer usedCount;

    @Schema(description = "Trạng thái coupon", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE", "EXPIRED"})
    private CouponStatus status;

    @Schema(description = "Loại coupon", example = "PUBLIC", allowableValues = {"PUBLIC", "PRIVATE"})
    private CouponType couponType;

    // === THÔNG TIN BỔ SUNG ===
    @Schema(description = "Thời gian tạo", example = "2025-01-15T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Thời gian cập nhật gần nhất", example = "2025-01-20T15:45:00")
    private LocalDateTime updatedAt;

    // === DANH SÁCH ÁP DỤNG ===
    @Schema(description = "Danh sách tên danh mục được áp dụng", example = "[\"Món chính\", \"Đồ uống\"]")
    private List<String> applicableCategoryNames;

    @Schema(description = "Danh sách tên món ăn được áp dụng", example = "[\"Phở bò\", \"Bún chả\"]")
    private List<String> applicableFoodNames;

    @Schema(description = "Số lượng user được áp dụng (với PRIVATE coupon)", example = "10")
    private Integer applicableUserCount;

    // === TRẠNG THÁI COMPUTED ===
    @Schema(description = "Coupon có còn hiệu lực không", example = "true")
    private Boolean isValid;

    @Schema(description = "Coupon sắp hết hạn (trong 7 ngày)", example = "false")
    private Boolean isExpiringSoon;

    @Schema(description = "Số lần còn lại có thể sử dụng", example = "55")
    private Integer remainingUsage;

    @Schema(description = "Phần trăm đã sử dụng", example = "45.0")
    private Double usagePercentage;

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
