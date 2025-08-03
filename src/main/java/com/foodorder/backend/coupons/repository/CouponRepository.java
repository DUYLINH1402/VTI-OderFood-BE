package com.foodorder.backend.coupons.repository;

import com.foodorder.backend.coupons.entity.Coupon;
import com.foodorder.backend.coupons.entity.CouponStatus;
import com.foodorder.backend.coupons.entity.CouponType;
import com.foodorder.backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

/**
 * Repository cho entity Coupon
 * Chỉ chứa các phương thức truy vấn dữ liệu, tuân thủ kiến trúc Spring Boot chuẩn
 */
@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    // === TÌM KIẾM CƠ BẢN ===
    /**
     * Tìm coupon theo mã code
     */
    Optional<Coupon> findByCode(String code);

    /**
     * Lấy danh sách coupon theo trạng thái
     */
    List<Coupon> findByStatus(CouponStatus status);

    /**
     * Lấy danh sách coupon theo loại
     */
    List<Coupon> findByCouponType(CouponType couponType);

    // === TÌM KIẾM NÂNG CAO ===
    /**
     * Tìm coupon công khai đang hoạt động (cho user thường)
     */
    @Query("SELECT c FROM Coupon c WHERE c.status = 'ACTIVE' " +
           "AND c.couponType = 'PUBLIC' " +
           "AND c.startDate <= :now AND c.endDate > :now " +
           "AND c.usedCount < c.maxUsage")
    List<Coupon> findActivePublicCoupons(@Param("now") LocalDateTime now);

    /**
     * Tìm coupon mà user có thể sử dụng (bao gồm PUBLIC và PRIVATE dành cho user đó)
     */
    @Query("SELECT DISTINCT c FROM Coupon c LEFT JOIN c.applicableUsers u " +
           "WHERE c.status = 'ACTIVE' " +
           "AND c.startDate <= :now AND c.endDate > :now " +
           "AND c.usedCount < c.maxUsage " +
           "AND (c.couponType = 'PUBLIC' OR (c.couponType = 'PRIVATE' AND u = :user))")
    List<Coupon> findAvailableCouponsForUser(@Param("user") User user, @Param("now") LocalDateTime now);

    /**
     * Tìm coupon theo khoảng giá trị giảm giá
     */
    @Query("SELECT c FROM Coupon c WHERE c.discountValue BETWEEN :minValue AND :maxValue")
    List<Coupon> findByDiscountValueBetween(@Param("minValue") Double minValue, @Param("maxValue") Double maxValue);

    /**
     * Tìm coupon sắp hết hạn (trong vòng X ngày)
     */
    @Query("SELECT c FROM Coupon c WHERE c.status = 'ACTIVE' AND c.endDate BETWEEN :now AND :futureDate")
    List<Coupon> findCouponsExpiringBetween(@Param("now") LocalDateTime now, @Param("futureDate") LocalDateTime futureDate);

    /**
     * Thống kê coupon theo trạng thái với phân trang
     */
    Page<Coupon> findByStatusAndTitleContainingIgnoreCase(CouponStatus status, String title, Pageable pageable);

    /**
     * Tìm coupon đã hết lượt sử dụng
     */
    @Query("SELECT c FROM Coupon c WHERE c.usedCount >= c.maxUsage")
    List<Coupon> findUsedOutCoupons();

    /**
     * Đếm số lượng coupon theo trạng thái
     */
    long countByStatus(CouponStatus status);
}
