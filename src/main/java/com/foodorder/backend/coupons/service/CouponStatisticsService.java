package com.foodorder.backend.coupons.service;

import com.foodorder.backend.coupons.dto.response.*;
import com.foodorder.backend.coupons.entity.CouponStatus;
import com.foodorder.backend.coupons.entity.CouponType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface cho thống kê và phân tích Coupon
 * Cung cấp các phương thức thống kê nâng cao cho admin dashboard
 */
public interface CouponStatisticsService {

    // === THỐNG KÊ TỔNG QUAN ===

    /**
     * Lấy thống kê tổng quan về coupon
     */
    CouponStatisticsResponse getOverallStatistics();

    // === PHÂN TÍCH SỬ DỤNG ===

    /**
     * Phân tích việc sử dụng coupon trong khoảng thời gian
     */
    CouponUsageAnalyticsResponse getUsageAnalytics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Phân tích hiệu suất của một coupon cụ thể
     */
    CouponPerformanceResponse getCouponPerformance(Long couponId);

    /**
     * Lấy top coupon hiệu quả nhất theo tiêu chí
     * @param criteria: USAGE (số lần dùng), DISCOUNT (tổng tiền giảm)
     * @param limit: Số lượng kết quả
     */
    List<CouponUsageAnalyticsResponse.TopCouponData> getTopCoupons(String criteria, int limit);

    // === BÁO CÁO THEO USER ===

    /**
     * Lấy thống kê sử dụng coupon của một user
     */
    UserCouponUsageResponse getUserCouponUsage(Long userId);

    /**
     * Lấy danh sách user sử dụng coupon nhiều nhất
     */
    List<UserCouponUsageResponse> getTopUsersByCouponUsage(int limit);

    // === LỌC VÀ TÌM KIẾM NÂNG CAO ===

    /**
     * Lọc danh sách coupon theo nhiều tiêu chí
     */
    Page<CouponPerformanceResponse> filterCoupons(CouponStatus status,
                                                    CouponType couponType,
                                                    String keyword,
                                                    Pageable pageable);

    // === XU HƯỚNG VÀ DỰ BÁO ===

    /**
     * Phân tích xu hướng sử dụng coupon theo thời gian
     */
    List<CouponUsageAnalyticsResponse.DailyUsageData> getUsageTrend(LocalDateTime startDate,
                                                                     LocalDateTime endDate);
}
