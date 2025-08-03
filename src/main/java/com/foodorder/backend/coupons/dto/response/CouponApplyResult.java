package com.foodorder.backend.coupons.dto.response;

import lombok.*;

/**
 * DTO cho kết quả áp dụng coupon
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponApplyResult {

    private Boolean success;
    private String message;
    private String couponCode;

    // Thông tin tính toán
    private Double originalAmount;
    private Double discountAmount;
    private Double finalAmount;
    private Double savedAmount; // Số tiền tiết kiệm được

    // Thông tin coupon
    private String couponTitle;
    private String discountDescription; // Ví dụ: "Giảm 20%" hoặc "Giảm 50,000đ"

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
