package com.foodorder.backend.order.dto.response;

import com.foodorder.backend.order.entity.DeliveryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response chứa thông tin chi tiết đơn hàng")
public class OrderResponse {

    @Schema(description = "ID nội bộ của đơn hàng", example = "1")
    private Long id;

    @Schema(description = "Mã đơn hàng hiển thị cho khách", example = "ORD-20250120-001")
    private String orderCode;

    @Schema(description = "ID của người đặt hàng", example = "1")
    private Long userId;

    @Schema(description = "Loại giao hàng", example = "DELIVERY")
    private DeliveryType deliveryType;

    @Schema(description = "Phương thức thanh toán", example = "COD")
    private String paymentMethod;

    @Schema(description = "ID quận/huyện", example = "1")
    private Long districtId;

    @Schema(description = "Tên quận/huyện", example = "Quận 1")
    private String districtName;

    @Schema(description = "ID phường/xã", example = "1")
    private Long wardId;

    @Schema(description = "Tên phường/xã", example = "Phường Bến Nghé")
    private String wardName;

    @Schema(description = "Địa chỉ giao hàng", example = "123 Nguyễn Huệ, Quận 1, TP.HCM")
    private String deliveryAddress;

    @Schema(description = "Tên người nhận", example = "Nguyễn Văn A")
    private String receiverName;

    @Schema(description = "Số điện thoại người nhận", example = "0901234567")
    private String receiverPhone;

    @Schema(description = "Email người nhận", example = "user@example.com")
    private String receiverEmail;

    @Schema(description = "Trạng thái đơn hàng", example = "CONFIRMED", allowableValues = {"PENDING", "CONFIRMED", "PREPARING", "READY", "SHIPPING", "DELIVERED", "CANCELLED"})
    private String status;

    @Schema(description = "Trạng thái thanh toán", example = "PAID", allowableValues = {"PENDING", "PAID", "FAILED", "REFUNDED"})
    private String paymentStatus;

    // === TIỀN TỆ MỚI - RÕ RÀNG ===
    @Schema(description = "Tổng tiền món ăn (không bao gồm phí ship, chưa trừ giảm giá)", example = "150000")
    private BigDecimal subtotalAmount;

    @Schema(description = "Phí giao hàng", example = "15000")
    private BigDecimal shippingFee;

    @Schema(description = "Tổng tiền sau khi cộng phí ship, trước khi áp dụng giảm giá", example = "165000")
    private BigDecimal totalBeforeDiscount;

    @Schema(description = "Số tiền cuối cùng khách phải trả", example = "145000")
    private BigDecimal finalAmount;

    // === GIẢM GIÁ ===
    @Schema(description = "Số điểm đã sử dụng", example = "100")
    private Integer pointsUsed;

    @Schema(description = "Số tiền giảm từ điểm thưởng", example = "10000")
    private BigDecimal pointsDiscountAmount;

    @Schema(description = "Mã coupon đã sử dụng", example = "SUMMER2025")
    private String couponCode;

    @Schema(description = "Số tiền giảm từ coupon", example = "10000")
    private BigDecimal couponDiscountAmount;

    // === DEPRECATED FIELDS - GIỮ LẠI ĐỂ TƯƠNG THÍCH ===
    @Deprecated
    @Schema(hidden = true)
    private Integer discountAmount;

    @Schema(description = "Thời gian tạo đơn", example = "2025-01-20T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Thời gian cập nhật gần nhất", example = "2025-01-20T10:35:00")
    private LocalDateTime updatedAt;

    @Schema(description = "Thời gian thanh toán", example = "2025-01-20T10:32:00")
    private LocalDateTime paymentTime;

    @Schema(description = "Mã giao dịch thanh toán", example = "TXN123456789")
    private String paymentTransactionId;

    // === MANAGEMENT FIELDS ===
    @Schema(description = "Ghi chú của nhân viên cho khách", example = "Giao trước 12h")
    private String staffNote;

    @Schema(description = "Ghi chú nội bộ (chỉ Staff/Admin thấy)", example = "Khách VIP")
    private String internalNote;

    @Schema(description = "Lý do hủy đơn (nếu có)", example = "Khách hàng yêu cầu hủy")
    private String cancelReason;

    @Schema(description = "Thời gian hủy đơn", example = "2025-01-20T11:00:00")
    private LocalDateTime cancelledAt;

    @Schema(description = "Danh sách các món ăn trong đơn hàng")
    private List<OrderItemResponse> items;

    // Item response lồng trong này luôn cho gọn
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Thông tin một món ăn trong đơn hàng")
    public static class OrderItemResponse {
        @Schema(description = "ID món ăn", example = "1")
        private Long foodId;

        @Schema(description = "Tên món ăn", example = "Phở bò tái")
        private String foodName;

        @Schema(description = "Slug của món ăn", example = "pho-bo-tai")
        private String foodSlug;

        @Schema(description = "URL hình ảnh", example = "https://example.com/pho.jpg")
        private String imageUrl;

        @Schema(description = "Số lượng", example = "2")
        private Integer quantity;

        @Schema(description = "Đơn giá", example = "55000")
        private BigDecimal price;
    }
}
