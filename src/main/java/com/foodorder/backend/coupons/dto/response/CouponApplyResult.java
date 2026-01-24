package com.foodorder.backend.coupons.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * DTO cho kết quả áp dụng coupon
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response chứa kết quả áp dụng mã giảm giá")
public class CouponApplyResult {

    @Schema(description = "Kết quả áp dụng coupon thành công hay không", example = "true")
    private Boolean success;

    @Schema(description = "Thông báo kết quả", example = "Coupon applied successfully")
    private String message;

    @Schema(description = "Mã coupon đã áp dụng", example = "SUMMER2025")
    private String couponCode;

    // Thông tin tính toán
    @Schema(description = "Giá trị đơn hàng gốc", example = "150000")
    private Double originalAmount;

    @Schema(description = "Số tiền được giảm", example = "30000")
    private Double discountAmount;

    @Schema(description = "Số tiền phải thanh toán sau khi giảm", example = "120000")
    private Double finalAmount;

    @Schema(description = "Số tiền tiết kiệm được", example = "30000")
    private Double savedAmount;

    // Thông tin coupon
    @Schema(description = "Tiêu đề coupon", example = "Khuyến mãi mùa hè")
    private String couponTitle;

    @Schema(description = "Mô tả chi tiết mức giảm", example = "Giảm 20%")
    private String discountDescription;

    public static CouponApplyResult success(String couponCode, String couponTitle,
                                          Double originalAmount, Double discountAmount) {
        return CouponApplyResult.builder()
            .success(true)
            .message("Coupon applied successfully")
            .couponCode(couponCode)
            .couponTitle(couponTitle)
            .originalAmount(originalAmount)
            .discountAmount(discountAmount)
            .finalAmount(originalAmount - discountAmount)
            .savedAmount(discountAmount)
            .build();
    }

    public static CouponApplyResult failure(String message) {
        return CouponApplyResult.builder()
            .success(false)
            .message(message)
            .build();
    }
}
