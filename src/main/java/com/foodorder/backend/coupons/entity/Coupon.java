package com.foodorder.backend.coupons.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import com.foodorder.backend.category.entity.Category;
import com.foodorder.backend.food.entity.Food;
import com.foodorder.backend.user.entity.User;

@Entity
@Table(name = "coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code; // Mã giảm giá

    @Column(length = 500)
    private String description; // Mô tả coupon

    @Column(length = 100)
    private String title; // Tiêu đề ngắn gọn của coupon

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType; // Loại giảm giá: PERCENT, AMOUNT

    @Column(nullable = false)
    private Double discountValue; // Giá trị giảm giá

    // === ĐIỀU KIỆN ÁP DỤNG NÂNG CAO ===
    @Column
    private Double minOrderAmount; // Giá trị đơn hàng tối thiểu để áp dụng coupon

    @Column
    private Double maxDiscountAmount; // Số tiền giảm tối đa (cho loại PERCENT)

    @Column
    private Integer maxUsagePerUser; // Số lần tối đa mỗi user có thể dùng coupon này

    @Column(nullable = false)
    private LocalDateTime startDate; // Ngày bắt đầu hiệu lực

    @Column(nullable = false)
    private LocalDateTime endDate;   // Ngày kết thúc hiệu lực

    @Column(nullable = false)
    private Integer maxUsage; // Số lần sử dụng tối đa

    @Column(nullable = false)
    private Integer usedCount = 0; // Số lần đã sử dụng, mặc định 0

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponStatus status; // Trạng thái coupon

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CouponType couponType = CouponType.PUBLIC; // Loại coupon: PUBLIC, PRIVATE, FIRST_ORDER

    // === QUAN HỆ VỚI CÁC ENTITY KHÁC ===
    /**
     * Coupon áp dụng cho các loại món (Category)
     */
    @ManyToMany
    @JoinTable(
        name = "coupon_categories",
        joinColumns = @JoinColumn(name = "coupon_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> applicableCategories;

    /**
     * Coupon áp dụng cho các món ăn cụ thể (Food)
     */
    @ManyToMany
    @JoinTable(
        name = "coupon_foods",
        joinColumns = @JoinColumn(name = "coupon_id"),
        inverseJoinColumns = @JoinColumn(name = "food_id")
    )
    private List<Food> applicableFoods;

    /**
     * Coupon chỉ áp dụng cho một số user nhất định (PRIVATE coupon)
     */
    @ManyToMany
    @JoinTable(
        name = "coupon_users",
        joinColumns = @JoinColumn(name = "coupon_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private List<User> applicableUsers;

    /**
     * Lịch sử sử dụng coupon
     */
    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CouponUsage> usageHistory;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // === BUSINESS METHODS ===
    /**
     * Kiểm tra coupon có còn hiệu lực không (chưa hết hạn, chưa hết lượt dùng)
     */
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return status == CouponStatus.ACTIVE
            && now.isAfter(startDate)
            && now.isBefore(endDate)
            && usedCount < maxUsage;
    }

    /**
     * Kiểm tra user có thể sử dụng coupon này không
     */
    public boolean canUserUseCoupon(User user, long userUsageCount) {
        // Kiểm tra coupon có hiệu lực không
        if (!isValid()) {
            return false;
        }

        // Nếu là PRIVATE coupon, chỉ user được chỉ định mới dùng được
        if (couponType == CouponType.PRIVATE && (applicableUsers == null || !applicableUsers.contains(user))) {
            return false;
        }

        // Kiểm tra số lần dùng tối đa của user
        return maxUsagePerUser == null || userUsageCount < maxUsagePerUser;
    }

    /**
     * Tính toán số tiền được giảm dựa trên giá trị đơn hàng
     */
    public double calculateDiscountAmount(double orderAmount) {
        if (minOrderAmount != null && orderAmount < minOrderAmount) {
            return 0;
        }

        double discount = 0;
        if (discountType == DiscountType.PERCENT) {
            discount = orderAmount * (discountValue / 100);
            // Áp dụng giảm giá tối đa nếu có
            if (maxDiscountAmount != null && discount > maxDiscountAmount) {
                discount = maxDiscountAmount;
            }
        } else if (discountType == DiscountType.AMOUNT) {
            discount = discountValue;
        }

        return Math.min(discount, orderAmount); // Không được giảm quá tổng đơn hàng
    }
}
