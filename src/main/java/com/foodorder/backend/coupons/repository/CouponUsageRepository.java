package com.foodorder.backend.coupons.repository;

import com.foodorder.backend.coupons.entity.Coupon;
import com.foodorder.backend.coupons.entity.CouponUsage;
import com.foodorder.backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // === THỐNG KÊ NÂNG CAO ===

    /**
     * Tính tổng số tiền đã giảm giá trong toàn hệ thống
     */
    @Query("SELECT COALESCE(SUM(cu.discountAmount), 0) FROM CouponUsage cu")
    Double getTotalDiscountAmount();

    /**
     * Tính tổng số tiền đã giảm giá trong khoảng thời gian
     */
    @Query("SELECT COALESCE(SUM(cu.discountAmount), 0) FROM CouponUsage cu WHERE cu.usedAt BETWEEN :startDate AND :endDate")
    Double getTotalDiscountAmountByDateRange(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    /**
     * Đếm số lần sử dụng coupon trong khoảng thời gian
     */
    @Query("SELECT COUNT(cu) FROM CouponUsage cu WHERE cu.usedAt BETWEEN :startDate AND :endDate")
    Long countByDateRange(@Param("startDate") LocalDateTime startDate,
                          @Param("endDate") LocalDateTime endDate);

    /**
     * Đếm số user duy nhất đã sử dụng coupon trong khoảng thời gian
     */
    @Query("SELECT COUNT(DISTINCT cu.user) FROM CouponUsage cu WHERE cu.usedAt BETWEEN :startDate AND :endDate")
    Long countUniqueUsersByDateRange(@Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    /**
     * Thống kê sử dụng coupon theo ngày trong khoảng thời gian
     */
    @Query("SELECT FUNCTION('DATE', cu.usedAt) as date, COUNT(cu), COALESCE(SUM(cu.discountAmount), 0) " +
           "FROM CouponUsage cu WHERE cu.usedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE', cu.usedAt) ORDER BY date")
    List<Object[]> getDailyUsageStatistics(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    /**
     * Lấy top coupon theo số tiền giảm giá
     */
    @Query("SELECT cu.coupon, COUNT(cu), COALESCE(SUM(cu.discountAmount), 0) " +
           "FROM CouponUsage cu GROUP BY cu.coupon ORDER BY SUM(cu.discountAmount) DESC")
    List<Object[]> getTopCouponsByDiscountAmount(Pageable pageable);

    /**
     * Lấy top coupon theo số lần sử dụng trong khoảng thời gian
     */
    @Query("SELECT cu.coupon, COUNT(cu), COALESCE(SUM(cu.discountAmount), 0) " +
           "FROM CouponUsage cu WHERE cu.usedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY cu.coupon ORDER BY COUNT(cu) DESC")
    List<Object[]> getTopCouponsByUsageInDateRange(@Param("startDate") LocalDateTime startDate,
                                                    @Param("endDate") LocalDateTime endDate,
                                                    Pageable pageable);

    /**
     * Thống kê sử dụng coupon theo loại coupon
     */
    @Query("SELECT cu.coupon.couponType, COUNT(cu), COALESCE(SUM(cu.discountAmount), 0) " +
           "FROM CouponUsage cu WHERE cu.usedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY cu.coupon.couponType")
    List<Object[]> getUsageStatisticsByCouponType(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate);

    /**
     * Lấy danh sách sử dụng coupon của một user cụ thể với phân trang
     */
    Page<CouponUsage> findByUserOrderByUsedAtDesc(User user, Pageable pageable);

    /**
     * Đếm tổng số lần sử dụng coupon của một user
     */
    Long countByUser(User user);

    /**
     * Tính tổng tiền giảm giá của một user
     */
    @Query("SELECT COALESCE(SUM(cu.discountAmount), 0) FROM CouponUsage cu WHERE cu.user = :user")
    Double getTotalDiscountAmountByUser(@Param("user") User user);

    /**
     * Đếm số user duy nhất đã sử dụng một coupon cụ thể
     */
    @Query("SELECT COUNT(DISTINCT cu.user) FROM CouponUsage cu WHERE cu.coupon = :coupon")
    Long countUniqueUsersByCoupon(@Param("coupon") Coupon coupon);

    /**
     * Tính tổng tiền giảm giá của một coupon cụ thể
     */
    @Query("SELECT COALESCE(SUM(cu.discountAmount), 0) FROM CouponUsage cu WHERE cu.coupon = :coupon")
    Double getTotalDiscountAmountByCoupon(@Param("coupon") Coupon coupon);
}
