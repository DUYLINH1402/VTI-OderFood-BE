package com.foodorder.backend.order.dto.response;

import com.foodorder.backend.order.entity.DeliveryType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id; // ID nội bộ
    private String orderCode; // Mã đơn hàng chính để hiển thị
    private Long userId;
    private DeliveryType deliveryType;
    private String paymentMethod;
    private Long districtId;
    private String districtName;
    private Long wardId;
    private String wardName;
    private String deliveryAddress;
    private String receiverName;
    private String receiverPhone;
    private String receiverEmail;
    private String status;
    private String paymentStatus;

    // === TIỀN TỆ MỚI - RÕ RÀNG ===
    private BigDecimal subtotalAmount;          // Tổng tiền món ăn (không bao gồm phí ship, chưa trừ giảm giá)
    private BigDecimal shippingFee;             // Phí giao hàng (nếu có)
    private BigDecimal totalBeforeDiscount;     // Tổng tiền sau khi cộng phí ship, trước khi áp dụng giảm giá
    private BigDecimal finalAmount;             // Số tiền cuối cùng khách phải trả (sau tất cả giảm giá)

    // === GIẢM GIÁ ===
    private Integer pointsUsed;                 // Số điểm đã sử dụng
    private BigDecimal pointsDiscountAmount;    // Số tiền giảm từ điểm thưởng
    private String couponCode;                  // Mã coupon đã sử dụng
    private BigDecimal couponDiscountAmount;    // Số tiền giảm từ coupon

    // === DEPRECATED FIELDS - GIỮ LẠI ĐỂ TƯƠNG THÍCH ===
    @Deprecated
    private Integer discountAmount;             // Deprecated: sử dụng pointsUsed thay thế

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime paymentTime;
    private String paymentTransactionId;

    // === MANAGEMENT FIELDS ===
    private String staffNote; // Ghi chú của nhân viên
    private String internalNote; // Ghi chú nội bộ
    private String cancelReason; // Lý do hủy đơn
    private LocalDateTime cancelledAt; // Thời gian hủy đơn

    private List<OrderItemResponse> items;

    // Item response lồng trong này luôn cho gọn
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemResponse {
        private Long foodId;
        private String foodName;
        private String foodSlug;
        private String imageUrl;
        private Integer quantity;
        private BigDecimal price;
    }
}
