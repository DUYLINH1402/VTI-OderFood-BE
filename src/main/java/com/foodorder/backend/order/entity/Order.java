package com.foodorder.backend.order.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
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

    @Column(name = "delivery_address", length = 255, nullable = false)
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

    @NotNull
    @Column(name = "total_price", precision = 38, scale = 2, nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "district_id", nullable = false)
    private Long districtId;

    @Column(name = "ward_id", nullable = false)
    private Long wardId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 20, nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "payment_time")
    private LocalDateTime paymentTime;

    @Column(name = "payment_transaction_id", length = 100)
    private String paymentTransactionId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> items;

    // Tự động set ngày giờ tạo/cập nhật
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}