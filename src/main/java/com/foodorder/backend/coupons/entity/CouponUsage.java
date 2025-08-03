package com.foodorder.backend.coupons.entity;

import com.foodorder.backend.order.entity.Order;
import com.foodorder.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Entity lưu lịch sử sử dụng coupon
 * Theo dõi ai đã dùng coupon nào, khi nào, cho đơn hàng nào
 */
@Entity
@Table(name = "coupon_usage")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponUsage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private Double discountAmount; // Số tiền thực tế được giảm

    @Column(nullable = false)
    private LocalDateTime usedAt; // Thời gian sử dụng

    @PrePersist
    protected void onCreate() {
        usedAt = LocalDateTime.now();
    }
}
