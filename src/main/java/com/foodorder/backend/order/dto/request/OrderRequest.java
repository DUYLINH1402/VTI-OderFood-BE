package com.foodorder.backend.order.dto.request;

import com.foodorder.backend.order.entity.DeliveryType;
import com.foodorder.backend.order.entity.PaymentMethod;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {
    private Long userId;
    private String receiverName;
    private String receiverPhone;
    private String receiverEmail;
    private String deliveryAddress;
    private Long shippingZoneId;
    private PaymentMethod paymentMethod;
    private DeliveryType deliveryType;
    // === TIỀN TỆ MỚI - RÕ RÀNG ===
    private BigDecimal subtotalAmount;          // Tổng tiền món ăn (không bao gồm phí ship, chưa trừ giảm giá)
    private BigDecimal shippingFee;             // Phí giao hàng (nếu có)
    private BigDecimal totalBeforeDiscount;     // Tổng tiền sau khi cộng phí ship, trước khi áp dụng giảm giá
    private BigDecimal finalAmount;             // Số tiền cuối cùng khách phải trả (sau tất cả giảm giá)
    // === GIẢM GIÁ ===
    private Integer pointsUsed;                 // Số điểm muốn sử dụng
    private BigDecimal pointsDiscountAmount;    // Số tiền giảm từ điểm thưởng (auto calculated)
    private String couponCode;                  // Mã coupon user muốn áp dụng
    private BigDecimal couponDiscountAmount;    // Số tiền giảm từ coupon (auto calculated)

    // === DEPRECATED FIELDS - GIỮ LẠI ĐỂ TƯƠNG THÍCH ===
    @Deprecated
    private BigDecimal totalPriceBeforeDiscount; // Deprecated: sử dụng subtotalAmount

    @Deprecated
    private BigDecimal totalPrice;               // Deprecated: sử dụng finalAmount

    @Deprecated
    private Integer discountAmount;              // Deprecated: sử dụng pointsUsed

    @Deprecated
    private BigDecimal originalAmount;           // Deprecated: sử dụng totalBeforeDiscount

    private Long districtId;
    private Long wardId;
    private List<OrderItemRequest> items;
}
