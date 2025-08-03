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
    private PaymentMethod paymentMethod; // Enum
    private DeliveryType deliveryType;
    private BigDecimal totalPrice;
    private Integer discountAmount;

    // === COUPON FIELDS ===
    private String couponCode; // Mã coupon user muốn áp dụng
    private BigDecimal couponDiscountAmount; // Số tiền giảm từ coupon (auto calculated)
    private BigDecimal originalAmount; // Tổng tiền gốc trước khi áp dụng coupon

    public Integer getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Integer discountAmount) {
        this.discountAmount = discountAmount;
    }

    private Long districtId;
    private Long wardId;
    private List<OrderItemRequest> items;
}
