package com.foodorder.backend.coupons.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO chứa thông tin sử dụng coupon của một User
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response chứa thông tin sử dụng mã giảm giá của người dùng")
public class UserCouponUsageResponse {

    // === THÔNG TIN USER ===
    @Schema(description = "ID của người dùng", example = "1")
    private Long userId;

    @Schema(description = "Tên đăng nhập", example = "johndoe")
    private String username;

    @Schema(description = "Họ tên đầy đủ", example = "Nguyễn Văn A")
    private String fullName;

    @Schema(description = "Email", example = "user@example.com")
    private String email;

    // === THỐNG KÊ TỔNG QUAN ===
    @Schema(description = "Tổng số coupon đã sử dụng", example = "10")
    private Long totalCouponsUsed;

    @Schema(description = "Tổng tiền đã được giảm (VND)", example = "500000")
    private Double totalDiscountReceived;

    @Schema(description = "Trung bình tiền giảm mỗi đơn (VND)", example = "50000")
    private Double averageDiscountPerOrder;

    // === LỊCH SỬ SỬ DỤNG ===
    @Schema(description = "Lịch sử sử dụng coupon")
    private List<CouponUsageDetail> usageHistory;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Chi tiết một lần sử dụng coupon")
    public static class CouponUsageDetail {
        @Schema(description = "ID của lần sử dụng", example = "1")
        private Long usageId;

        @Schema(description = "ID của coupon", example = "5")
        private Long couponId;

        @Schema(description = "Mã coupon", example = "SUMMER2025")
        private String couponCode;

        @Schema(description = "Tiêu đề coupon", example = "Khuyến mãi mùa hè")
        private String couponTitle;

        @Schema(description = "ID đơn hàng", example = "100")
        private Long orderId;

        @Schema(description = "Số tiền đã giảm (VND)", example = "50000")
        private Double discountAmount;

        @Schema(description = "Thời gian sử dụng", example = "2025-01-15T14:30:00")
        private LocalDateTime usedAt;
    }
}

