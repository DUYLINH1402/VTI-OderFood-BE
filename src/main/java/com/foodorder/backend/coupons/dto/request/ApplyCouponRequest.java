package com.foodorder.backend.coupons.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;

/**
 * DTO cho request áp dụng coupon vào đơn hàng
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyCouponRequest {

    @NotBlank(message = "Coupon code must not be blank")
    private String couponCode;

    @NotNull(message = "Order amount must not be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Order amount must be greater than 0")
    private Double orderAmount;

    @NotNull(message = "User ID must not be null")
    private Long userId;

    // Danh sách ID món ăn trong đơn hàng (để kiểm tra coupon có áp dụng được không)
    private java.util.List<Long> foodIds;
}
