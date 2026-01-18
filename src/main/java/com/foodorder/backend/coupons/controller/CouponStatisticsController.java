package com.foodorder.backend.coupons.controller;

import com.foodorder.backend.coupons.dto.response.*;
import com.foodorder.backend.coupons.entity.CouponStatus;
import com.foodorder.backend.coupons.entity.CouponType;
import com.foodorder.backend.coupons.service.CouponStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller cho API thống kê và phân tích Coupon
 * Cung cấp các endpoint cho admin dashboard để quản lý hiệu quả chương trình khuyến mãi
 */
@RestController
@RequestMapping("/api/v1/admin/promotions/coupons")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class CouponStatisticsController {

    private final CouponStatisticsService couponStatisticsService;

    // ============ THỐNG KÊ TỔNG QUAN ============

    /**
     * API lấy thống kê tổng quan về coupon
     * GET /api/v1/admin/promotions/coupons/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<CouponStatisticsResponse> getOverallStatistics() {
        CouponStatisticsResponse statistics = couponStatisticsService.getOverallStatistics();
        return ResponseEntity.ok(statistics);
    }

    // ============ PHÂN TÍCH SỬ DỤNG ============

    /**
     * API phân tích việc sử dụng coupon trong khoảng thời gian
     * GET /api/v1/admin/promotions/coupons/analytics?startDate=...&endDate=...
     */
    @GetMapping("/analytics")
    public ResponseEntity<CouponUsageAnalyticsResponse> getUsageAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        CouponUsageAnalyticsResponse analytics = couponStatisticsService.getUsageAnalytics(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    /**
     * API lấy xu hướng sử dụng coupon theo ngày
     * GET /api/v1/admin/promotions/coupons/trend?startDate=...&endDate=...
     */
    @GetMapping("/trend")
    public ResponseEntity<List<CouponUsageAnalyticsResponse.DailyUsageData>> getUsageTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<CouponUsageAnalyticsResponse.DailyUsageData> trend = couponStatisticsService.getUsageTrend(startDate, endDate);
        return ResponseEntity.ok(trend);
    }

    // ============ HIỆU SUẤT COUPON ============

    /**
     * API lấy hiệu suất của một coupon cụ thể
     * GET /api/v1/admin/promotions/coupons/{couponId}/performance
     */
    @GetMapping("/{couponId}/performance")
    public ResponseEntity<CouponPerformanceResponse> getCouponPerformance(@PathVariable Long couponId) {
        CouponPerformanceResponse performance = couponStatisticsService.getCouponPerformance(couponId);
        return ResponseEntity.ok(performance);
    }

    /**
     * API lấy top coupon hiệu quả nhất
     * GET /api/v1/admin/promotions/coupons/top?criteria=USAGE&limit=10
     * @param criteria: USAGE (số lần dùng) hoặc DISCOUNT (tổng tiền giảm)
     */
    @GetMapping("/top")
    public ResponseEntity<List<CouponUsageAnalyticsResponse.TopCouponData>> getTopCoupons(
            @RequestParam(defaultValue = "USAGE") String criteria,
            @RequestParam(defaultValue = "10") int limit) {
        List<CouponUsageAnalyticsResponse.TopCouponData> topCoupons = couponStatisticsService.getTopCoupons(criteria, limit);
        return ResponseEntity.ok(topCoupons);
    }

    // ============ BÁO CÁO THEO USER ============

    /**
     * API lấy thống kê sử dụng coupon của một user
     * GET /api/v1/admin/promotions/coupons/users/{userId}
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserCouponUsageResponse> getUserCouponUsage(@PathVariable Long userId) {
        UserCouponUsageResponse userUsage = couponStatisticsService.getUserCouponUsage(userId);
        return ResponseEntity.ok(userUsage);
    }

    /**
     * API lấy top user sử dụng coupon nhiều nhất
     * GET /api/v1/admin/promotions/coupons/users/top?limit=10
     */
    @GetMapping("/users/top")
    public ResponseEntity<List<UserCouponUsageResponse>> getTopUsersByCouponUsage(
            @RequestParam(defaultValue = "10") int limit) {
        List<UserCouponUsageResponse> topUsers = couponStatisticsService.getTopUsersByCouponUsage(limit);
        return ResponseEntity.ok(topUsers);
    }

    // ============ LỌC VÀ TÌM KIẾM ============

    /**
     * API lọc danh sách coupon với nhiều tiêu chí
     * GET /api/v1/admin/promotions/coupons/filter?status=ACTIVE&type=PUBLIC&keyword=...
     */
    @GetMapping("/filter")
    public ResponseEntity<Page<CouponPerformanceResponse>> filterCoupons(
            @RequestParam(required = false) CouponStatus status,
            @RequestParam(required = false) CouponType type,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<CouponPerformanceResponse> coupons = couponStatisticsService.filterCoupons(status, type, keyword, pageable);
        return ResponseEntity.ok(coupons);
    }

    // ============ DASHBOARD TỔNG HỢP ============

    /**
     * API lấy dashboard tổng hợp cho quản lý coupon
     * GET /api/v1/admin/promotions/coupons/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getCouponDashboard() {

        // Lấy thống kê tổng quan
        CouponStatisticsResponse statistics = couponStatisticsService.getOverallStatistics();

        // Lấy top coupon
        List<CouponUsageAnalyticsResponse.TopCouponData> topByUsage = couponStatisticsService.getTopCoupons("USAGE", 5);
        List<CouponUsageAnalyticsResponse.TopCouponData> topByDiscount = couponStatisticsService.getTopCoupons("DISCOUNT", 5);

        // Lấy xu hướng 30 ngày gần đây
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(30);
        List<CouponUsageAnalyticsResponse.DailyUsageData> trend = couponStatisticsService.getUsageTrend(startDate, endDate);

        // Lấy top users
        List<UserCouponUsageResponse> topUsers = couponStatisticsService.getTopUsersByCouponUsage(5);

        Map<String, Object> dashboard = Map.of(
                "statistics", statistics,
                "topCouponsByUsage", topByUsage,
                "topCouponsByDiscount", topByDiscount,
                "usageTrend30Days", trend,
                "topUsersByCouponUsage", topUsers
        );

        return ResponseEntity.ok(dashboard);
    }
}

