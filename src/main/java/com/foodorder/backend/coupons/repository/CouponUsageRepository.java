package com.foodorder.backend.coupons.repository;

import com.foodorder.backend.coupons.entity.Coupon;
import com.foodorder.backend.coupons.entity.CouponUsage;
import com.foodorder.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository cho CouponUsage - quản lý lịch sử sử dụng coupon
 */
@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {

    /**
     * Đếm số lần user đã sử dụng một coupon cụ thể
     */
    long countByCouponAndUser(Coupon coupon, User user);

    /**
     * Lấy danh sách lịch sử sử dụng coupon của user
     */
    List<CouponUsage> findByUserOrderByUsedAtDesc(User user);

    /**
     * Lấy danh sách lịch sử sử dụng của một coupon
     */
    List<CouponUsage> findByCouponOrderByUsedAtDesc(Coupon coupon);

    /**
     * Thống kê doanh thu tiết kiệm theo coupon trong khoảng thời gian
     */
    @Query("SELECT SUM(cu.discountAmount) FROM CouponUsage cu WHERE cu.coupon = :coupon AND cu.usedAt BETWEEN :startDate AND :endDate")
    Double getTotalDiscountAmountByCouponAndDateRange(@Param("coupon") Coupon coupon,
                                                     @Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Kiểm tra user đã sử dụng coupon trong khoảng thời gian nhất định chưa
     */
    boolean existsByCouponAndUserAndUsedAtBetween(Coupon coupon, User user, LocalDateTime startDate, LocalDateTime endDate);
}
