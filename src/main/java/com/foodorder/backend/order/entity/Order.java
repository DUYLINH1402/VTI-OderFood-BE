package com.foodorder.backend.order.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.*;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = true)
    private Long userId;

    @NotBlank
    @Column(name = "receiver_name", length = 100, nullable = false)
    private String receiverName;

    @NotBlank
    @Column(name = "receiver_phone", length = 20, nullable = false)
    private String receiverPhone;

    @NotBlank
    @Column(name = "receiver_email", length = 100, nullable = false)
    private String receiverEmail;

    @Column(name = "delivery_address", length = 255, nullable = true)
    private String deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20, nullable = false)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_type", length = 20, nullable = false)
    private DeliveryType deliveryType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private OrderStatus status;

    // === TIỀN TỆ MỚI - RÕ RÀNG ===
    @NotNull
    @Column(name = "subtotal_amount", precision = 38, scale = 2, nullable = false)
    private BigDecimal subtotalAmount; // Tổng tiền món ăn (không bao gồm phí ship, chưa trừ giảm giá)

    @Column(name = "shipping_fee", precision = 10, scale = 2)
    private BigDecimal shippingFee; // Phí giao hàng (nếu có)

    @Column(name = "total_before_discount", precision = 38, scale = 2)
    private BigDecimal totalBeforeDiscount; // Tổng tiền sau khi cộng phí ship, trước khi áp dụng giảm giá

    @NotNull
    @Column(name = "final_amount", precision = 38, scale = 2, nullable = false)
    private BigDecimal finalAmount; // Số tiền cuối cùng khách phải trả (sau tất cả giảm giá)

    // === GIẢM GIÁ ===
    @Column(name = "points_used")
    private Integer pointsUsed; // Số điểm đã sử dụng

    @Column(name = "points_discount_amount", precision = 10, scale = 2)
    private BigDecimal pointsDiscountAmount; // Số tiền giảm từ điểm thưởng

    @Column(name = "coupon_code", length = 50)
    private String couponCode; // Mã coupon đã sử dụng

    @Column(name = "coupon_discount_amount", precision = 10, scale = 2)
    private BigDecimal couponDiscountAmount; // Số tiền giảm từ coupon

    // === DEPRECATED FIELDS - SẼ XÓA SAU KHI MIGRATE ===
    @Deprecated
    @Column(name = "total_food_price", precision = 38, scale = 2)
    private BigDecimal totalFoodPrice; // Deprecated: sử dụng subtotalAmount thay thế

    @Deprecated
    @Column(name = "total_price", precision = 38, scale = 2)
    private BigDecimal totalPrice; // Deprecated: sử dụng finalAmount thay thế

    @Deprecated
    @Column(name = "discount_amount")
    private Integer discountAmount; // Deprecated: sử dụng pointsUsed thay thế

    @Deprecated
    @Column(name = "original_amount", precision = 10, scale = 2)
    private BigDecimal originalAmount; // Deprecated: sử dụng totalBeforeDiscount thay thế

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "district_id", nullable = true)
    private Long districtId;

    @Column(name = "ward_id", nullable = true)
    private Long wardId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20, nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "payment_time")
    private LocalDateTime paymentTime;

    @Column(name = "payment_transaction_id", length = 100)
    private String paymentTransactionId;

    // === MANAGEMENT FIELDS ===
    @Column(name = "staff_note", columnDefinition = "TEXT")
    private String staffNote; // Ghi chú của nhân viên

    @Column(name = "internal_note", columnDefinition = "TEXT")
    private String internalNote; // Ghi chú nội bộ

    @Column(name = "cancel_reason", length = 255)
    private String cancelReason; // Lý do hủy đơn

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt; // Thời gian hủy đơn

    @Column(name = "order_code", length = 50, unique = true)
    private String orderCode; // Mã đơn hàng để tra cứu

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items;

    // Tự động set ngày giờ tạo/cập nhật và tạo order code
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // Tự động tạo order code nếu chưa có
        if (this.orderCode == null || this.orderCode.isEmpty()) {
            this.orderCode = generateOrderCode();
        }
    }

    // Method để tạo order code duy nhất
    private String generateOrderCode() {
        // Format: DGX + timestamp + random số để đảm bảo unique
        long timestamp = System.currentTimeMillis() % 1000000; // Lấy 6 chữ số cuối
        int random = (int) (Math.random() * 1000); // Random 0-999
        return String.format("DGX%06d%03d", timestamp, random);
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}