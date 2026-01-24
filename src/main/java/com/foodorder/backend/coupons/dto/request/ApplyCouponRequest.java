package com.foodorder.backend.coupons.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.*;

import java.util.List;

/**
 * DTO cho request áp dụng coupon vào đơn hàng
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body để áp dụng mã giảm giá vào đơn hàng")
public class ApplyCouponRequest {

    @Schema(
        description = "Mã coupon cần áp dụng",
        example = "SUMMER2025",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Coupon code must not be blank")
    private String couponCode;

    @Schema(
        description = "Tổng giá trị đơn hàng trước khi giảm giá",
        example = "150000",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Order amount must not be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Order amount must be greater than 0")
    private Double orderAmount;

    @Schema(description = "ID của người dùng áp dụng coupon", example = "1")
    private Long userId;

    @Schema(description = "Danh sách ID các món ăn trong đơn hàng", example = "[1, 2, 3]")
    private List<Long> foodIds;
}
