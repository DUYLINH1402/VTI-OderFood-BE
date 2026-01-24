package com.foodorder.backend.order.dto.request;

import com.foodorder.backend.order.entity.DeliveryType;
import com.foodorder.backend.order.entity.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body để tạo đơn hàng mới")
public class OrderRequest {

    @Schema(description = "ID của người dùng đặt hàng", example = "1")
    private Long userId;

    @Schema(description = "Tên người nhận", example = "Nguyễn Văn A", requiredMode = Schema.RequiredMode.REQUIRED)
    private String receiverName;

    @Schema(description = "Số điện thoại người nhận", example = "0901234567", requiredMode = Schema.RequiredMode.REQUIRED)
    private String receiverPhone;

    @Schema(description = "Email người nhận (để gửi thông báo)", example = "user@example.com")
    private String receiverEmail;

    @Schema(description = "Địa chỉ giao hàng", example = "123 Nguyễn Huệ, Quận 1, TP.HCM", requiredMode = Schema.RequiredMode.REQUIRED)
    private String deliveryAddress;

    @Schema(description = "ID khu vực giao hàng", example = "1")
    private Long shippingZoneId;

    @Schema(description = "Phương thức thanh toán", example = "COD", allowableValues = {"COD", "BANK_TRANSFER", "VNPAY"})
    private PaymentMethod paymentMethod;

    @Schema(description = "Loại giao hàng", example = "DELIVERY", allowableValues = {"DELIVERY", "PICKUP"})
    private DeliveryType deliveryType;

    // === TIỀN TỆ MỚI - RÕ RÀNG ===
    @Schema(description = "Tổng tiền món ăn (không bao gồm phí ship, chưa trừ giảm giá)", example = "150000")
    private BigDecimal subtotalAmount;

    @Schema(description = "Phí giao hàng", example = "15000")
    private BigDecimal shippingFee;

    @Schema(description = "Tổng tiền sau khi cộng phí ship, trước khi áp dụng giảm giá", example = "165000")
    private BigDecimal totalBeforeDiscount;

    @Schema(description = "Số tiền cuối cùng khách phải trả (sau tất cả giảm giá)", example = "145000")
    private BigDecimal finalAmount;

    // === GIẢM GIÁ ===
    @Schema(description = "Số điểm thưởng muốn sử dụng", example = "100")
    private Integer pointsUsed;

    @Schema(description = "Số tiền giảm từ điểm thưởng (tự động tính)", example = "10000")
    private BigDecimal pointsDiscountAmount;

    @Schema(description = "Mã coupon muốn áp dụng", example = "SUMMER2025")
    private String couponCode;

    @Schema(description = "Số tiền giảm từ coupon (tự động tính)", example = "10000")
    private BigDecimal couponDiscountAmount;

    // === DEPRECATED FIELDS - GIỮ LẠI ĐỂ TƯƠNG THÍCH ===
    @Deprecated
    @Schema(hidden = true)
    private BigDecimal totalPriceBeforeDiscount;

    @Deprecated
    @Schema(hidden = true)
    private BigDecimal totalPrice;

    @Deprecated
    @Schema(hidden = true)
    private Integer discountAmount;

    @Deprecated
    @Schema(hidden = true)
    private BigDecimal originalAmount;

    @Schema(description = "ID quận/huyện", example = "1")
    private Long districtId;

    @Schema(description = "ID phường/xã", example = "1")
    private Long wardId;

    @Schema(description = "Danh sách các món ăn trong đơn hàng", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<OrderItemRequest> items;
}
