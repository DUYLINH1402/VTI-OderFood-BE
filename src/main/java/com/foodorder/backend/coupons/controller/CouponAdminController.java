package com.foodorder.backend.coupons.controller;

import com.foodorder.backend.coupons.dto.request.CouponRequest;
import com.foodorder.backend.coupons.dto.response.*;
import com.foodorder.backend.coupons.entity.CouponStatus;
import com.foodorder.backend.coupons.entity.CouponType;
import com.foodorder.backend.coupons.service.CouponService;
import com.foodorder.backend.coupons.service.CouponStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Admin Controller cho quản lý Coupon
 * Bao gồm: CRUD, quản lý trạng thái, thống kê, phân tích và campaign
 * Các chức năng dành riêng cho admin/manager
 */
@RestController
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
@Validated
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
@Tag(name = "Admin Coupons", description = "API quản lý mã giảm giá dành cho Admin")
public class CouponAdminController {

    private final CouponService couponService;
    private final CouponStatisticsService couponStatisticsService;

    // ============ QUẢN LÝ COUPON CƠ BẢN (CRUD) ============

    /**
     * Tạo mới coupon
     * POST /api/admin/coupons
     */
    @Operation(summary = "Tạo mới coupon", description = "Tạo mã giảm giá mới.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tạo thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "403", description = "Không có quyền Admin")
    })
    @PostMapping
    public ResponseEntity<CouponResponse> createCoupon(@RequestBody @Valid CouponRequest request) {
        CouponResponse response = couponService.createCoupon(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật coupon
     * PUT /api/admin/coupons/{id}
     */
    @Operation(summary = "Cập nhật coupon", description = "Cập nhật thông tin mã giảm giá.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy coupon")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CouponResponse> updateCoupon(
            @Parameter(description = "ID của coupon") @PathVariable Long id,
            @RequestBody @Valid CouponRequest request) {
        CouponResponse response = couponService.updateCoupon(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa coupon (soft delete)
     * DELETE /api/admin/coupons/{id}
     */
    @Operation(summary = "Xóa coupon", description = "Xóa mã giảm giá (soft delete).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Xóa thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy coupon")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(
            @Parameter(description = "ID của coupon") @PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lấy chi tiết coupon theo ID
     * GET /api/admin/coupons/{id}
     */
    @Operation(summary = "Chi tiết coupon (ID)", description = "Lấy thông tin chi tiết mã giảm giá theo ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy coupon")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CouponResponse> getCouponById(
            @Parameter(description = "ID của coupon") @PathVariable Long id) {
        return couponService.getCouponById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lấy danh sách tất cả coupon với phân trang
     * GET /api/admin/coupons
     */
    @Operation(summary = "Danh sách tất cả coupon", description = "Lấy danh sách tất cả mã giảm giá với phân trang.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping
    public ResponseEntity<Page<CouponResponse>> getAllCoupons(
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp") @RequestParam(defaultValue = "desc") String sortDir) {

        Pageable pageable = PageRequest.of(page, size,
                sortDir.equalsIgnoreCase("desc")
                        ? Sort.by(sortBy).descending()
                        : Sort.by(sortBy).ascending());

        Page<CouponResponse> response = couponService.getAllCoupons(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách coupon theo trạng thái
     * GET /api/admin/coupons/status/{status}
     */
    @Operation(summary = "Danh sách coupon theo trạng thái", description = "Lấy danh sách mã giảm giá theo trạng thái.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/status/{status}")
    public ResponseEntity<List<CouponResponse>> getCouponsByStatus(
            @Parameter(description = "Trạng thái coupon") @PathVariable CouponStatus status) {
        List<CouponResponse> response = couponService.getCouponsByStatus(status);
        return ResponseEntity.ok(response);
    }

    // ============ QUẢN LÝ TRẠNG THÁI COUPON ============

    /**
     * Kích hoạt coupon
     * PUT /api/admin/coupons/{id}/activate
     */
    @Operation(summary = "Kích hoạt coupon", description = "Kích hoạt mã giảm giá.")
    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activateCoupon(@PathVariable Long id) {
        couponService.activateCoupon(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Vô hiệu hóa coupon
     * PUT /api/admin/coupons/{id}/deactivate
     */
    @Operation(summary = "Vô hiệu hóa coupon", description = "Vô hiệu hóa mã giảm giá.")
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateCoupon(@PathVariable Long id) {
        couponService.deactivateCoupon(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Cập nhật trạng thái coupon hết hạn (manual trigger)
     * PUT /api/admin/coupons/update-expired
     */
    @Operation(summary = "Cập nhật coupon hết hạn", description = "Cập nhật trạng thái các coupon đã hết hạn.")
    @PutMapping("/update-expired")
    public ResponseEntity<Void> updateExpiredCoupons() {
        couponService.updateExpiredCoupons();
        return ResponseEntity.ok().build();
    }

    /**
     * Cập nhật trạng thái coupon hết lượt sử dụng (manual trigger)
     * PUT /api/admin/coupons/update-used-out
     */
    @Operation(summary = "Cập nhật coupon hết lượt", description = "Cập nhật trạng thái các coupon đã hết lượt sử dụng.")
    @PutMapping("/update-used-out")
    public ResponseEntity<Void> updateUsedOutCoupons() {
        couponService.updateUsedOutCoupons();
        return ResponseEntity.ok().build();
    }

    // ============ THỐNG KÊ TỔNG QUAN ============

    /**
     * Lấy thống kê tổng quan về coupon
     * GET /api/admin/coupons/statistics
     */
    @Operation(summary = "Thống kê tổng quan coupon", description = "Lấy thống kê tổng quan về tất cả coupon.")
    @GetMapping("/statistics")
    public ResponseEntity<CouponStatisticsResponse> getOverallStatistics() {
        CouponStatisticsResponse statistics = couponStatisticsService.getOverallStatistics();
        return ResponseEntity.ok(statistics);
    }

    /**
     * Thống kê coupon theo trạng thái (đếm số lượng)
     * GET /api/admin/coupons/statistics/by-status
     */
    @Operation(summary = "Thống kê theo trạng thái", description = "Đếm số lượng coupon theo từng trạng thái.")
    @GetMapping("/statistics/by-status")
    public ResponseEntity<Map<CouponStatus, Long>> getCouponStatisticsByStatus() {
        Map<CouponStatus, Long> stats = couponService.getCouponStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Top coupon được sử dụng nhiều nhất
     * GET /api/admin/coupons/most-used?limit=10
     */
    @Operation(summary = "Top coupon sử dụng nhiều", description = "Lấy danh sách coupon được sử dụng nhiều nhất.")
    @GetMapping("/most-used")
    public ResponseEntity<List<CouponResponse>> getMostUsedCoupons(
            @RequestParam(defaultValue = "10") @Min(1) int limit) {
        List<CouponResponse> response = couponService.getMostUsedCoupons(limit);
        return ResponseEntity.ok(response);
    }

    /**
     * Danh sách coupon sắp hết hạn
     * GET /api/admin/coupons/expiring-soon?days=7
     */
    @Operation(summary = "Coupon sắp hết hạn", description = "Lấy danh sách coupon sắp hết hạn trong N ngày.")
    @GetMapping("/expiring-soon")
    public ResponseEntity<List<CouponResponse>> getExpiringSoonCoupons(
            @RequestParam(defaultValue = "7") @Min(1) int days) {
        List<CouponResponse> response = couponService.getExpiringSoonCoupons(days);
        return ResponseEntity.ok(response);
    }

    // ============ PHÂN TÍCH SỬ DỤNG ============

    /**
     * Phân tích việc sử dụng coupon trong khoảng thời gian
     * GET /api/admin/coupons/analytics?startDate=...&endDate=...
     */
    @Operation(summary = "Phân tích sử dụng coupon", description = "Phân tích chi tiết việc sử dụng coupon trong khoảng thời gian.")
    @GetMapping("/analytics")
    public ResponseEntity<CouponUsageAnalyticsResponse> getUsageAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        CouponUsageAnalyticsResponse analytics = couponStatisticsService.getUsageAnalytics(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    /**
     * Lấy xu hướng sử dụng coupon theo ngày
     * GET /api/admin/coupons/trend?startDate=...&endDate=...
     */
    @Operation(summary = "Xu hướng sử dụng coupon", description = "Lấy xu hướng sử dụng coupon theo từng ngày.")
    @GetMapping("/trend")
    public ResponseEntity<List<CouponUsageAnalyticsResponse.DailyUsageData>> getUsageTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<CouponUsageAnalyticsResponse.DailyUsageData> trend = couponStatisticsService.getUsageTrend(startDate, endDate);
        return ResponseEntity.ok(trend);
    }

    // ============ HIỆU SUẤT COUPON ============

    /**
     * Lấy hiệu suất của một coupon cụ thể
     * GET /api/admin/coupons/{couponId}/performance
     */
    @Operation(summary = "Hiệu suất coupon", description = "Lấy thông tin hiệu suất chi tiết của một coupon.")
    @GetMapping("/{couponId}/performance")
    public ResponseEntity<CouponPerformanceResponse> getCouponPerformance(@PathVariable Long couponId) {
        CouponPerformanceResponse performance = couponStatisticsService.getCouponPerformance(couponId);
        return ResponseEntity.ok(performance);
    }

    /**
     * Lấy top coupon hiệu quả nhất
     * GET /api/admin/coupons/top?criteria=USAGE&limit=10
     * @param criteria: USAGE (số lần dùng) hoặc DISCOUNT (tổng tiền giảm)
     */
    @Operation(summary = "Top coupon hiệu quả", description = "Lấy top coupon hiệu quả nhất theo tiêu chí.")
    @GetMapping("/top")
    public ResponseEntity<List<CouponUsageAnalyticsResponse.TopCouponData>> getTopCoupons(
            @RequestParam(defaultValue = "USAGE") String criteria,
            @RequestParam(defaultValue = "10") int limit) {
        List<CouponUsageAnalyticsResponse.TopCouponData> topCoupons = couponStatisticsService.getTopCoupons(criteria, limit);
        return ResponseEntity.ok(topCoupons);
    }

    // ============ BÁO CÁO THEO USER ============

    /**
     * Lấy thống kê sử dụng coupon của một user
     * GET /api/admin/coupons/users/{userId}
     */
    @Operation(summary = "Thống kê coupon của user", description = "Lấy thống kê chi tiết việc sử dụng coupon của một user.")
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserCouponUsageResponse> getUserCouponUsage(@PathVariable Long userId) {
        UserCouponUsageResponse userUsage = couponStatisticsService.getUserCouponUsage(userId);
        return ResponseEntity.ok(userUsage);
    }

    /**
     * Lấy top user sử dụng coupon nhiều nhất
     * GET /api/admin/coupons/users/top?limit=10
     */
    @Operation(summary = "Top user sử dụng coupon", description = "Lấy danh sách user sử dụng coupon nhiều nhất.")
    @GetMapping("/users/top")
    public ResponseEntity<List<UserCouponUsageResponse>> getTopUsersByCouponUsage(
            @RequestParam(defaultValue = "10") int limit) {
        List<UserCouponUsageResponse> topUsers = couponStatisticsService.getTopUsersByCouponUsage(limit);
        return ResponseEntity.ok(topUsers);
    }

    // ============ LỌC VÀ TÌM KIẾM ============

    /**
     * Lọc danh sách coupon với nhiều tiêu chí
     * GET /api/admin/coupons/filter?status=ACTIVE&type=PUBLIC&keyword=...
     */
    @Operation(summary = "Lọc coupon", description = "Lọc danh sách coupon theo nhiều tiêu chí.")
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
     * Dashboard tổng quan coupon
     * GET /api/admin/coupons/dashboard
     */
    @Operation(summary = "Dashboard coupon", description = "Lấy dashboard tổng hợp cho quản lý coupon.")
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getCouponDashboard() {
        log.info("Admin requested coupon dashboard");

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

        // Lấy coupon sắp hết hạn
        List<CouponResponse> expiringSoon = couponService.getExpiringSoonCoupons(7);

        Map<String, Object> dashboard = Map.of(
                "statistics", statistics,
                "topCouponsByUsage", topByUsage,
                "topCouponsByDiscount", topByDiscount,
                "usageTrend30Days", trend,
                "topUsersByCouponUsage", topUsers,
                "expiringSoonCoupons", expiringSoon
        );

        return ResponseEntity.ok(dashboard);
    }

    // ============ ADVANCED REPORTS ============

    /**
     * Báo cáo chi tiết theo khoảng thời gian
     * GET /api/admin/coupons/detailed-report?startDate=2024-01-01&endDate=2024-01-31
     */
    @Operation(summary = "Báo cáo chi tiết", description = "Lấy báo cáo chi tiết coupon theo khoảng thời gian.")
    @GetMapping("/detailed-report")
    public ResponseEntity<Map<String, Object>> getDetailedReport(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        log.info("Admin requested detailed report from {} to {}", startDate, endDate);

        var statistics = couponService.getCouponStatistics();
        var mostUsed = couponService.getMostUsedCoupons(10);

        Map<String, Object> report = Map.of(
                "reportPeriod", Map.of("startDate", startDate, "endDate", endDate),
                "statistics", statistics,
                "topCoupons", mostUsed,
                "generatedAt", java.time.LocalDateTime.now().toString()
        );

        return ResponseEntity.ok(report);
    }

    // ============ INTERNAL API (CHO ORDER SERVICE) ============

    /**
     * Xác nhận sử dụng coupon (được gọi từ Order Service)
     * POST /api/admin/coupons/confirm-usage
     */
    @Operation(summary = "Xác nhận sử dụng coupon", description = "API nội bộ để xác nhận việc sử dụng coupon từ Order Service.")
    @PostMapping("/confirm-usage")
    public ResponseEntity<Void> confirmCouponUsage(
            @RequestParam String couponCode,
            @RequestParam Long userId,
            @RequestParam Long orderId,
            @RequestParam Double discountAmount) {
        couponService.confirmCouponUsage(couponCode, userId, orderId, discountAmount);
        return ResponseEntity.ok().build();
    }

    /**
     * Hủy sử dụng coupon (khi đơn hàng bị hủy)
     * DELETE /api/admin/coupons/usage/{usageId}
     */
    @Operation(summary = "Hủy sử dụng coupon", description = "Hủy việc sử dụng coupon khi đơn hàng bị hủy.")
    @DeleteMapping("/usage/{usageId}")
    public ResponseEntity<Void> cancelCouponUsage(@PathVariable Long usageId) {
        couponService.cancelCouponUsage(usageId);
        return ResponseEntity.ok().build();
    }

    // ============ EXCEPTION HANDLING ============

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        log.error("Admin controller error: {}", e.getMessage(), e);
        return ResponseEntity.badRequest()
                .body(Map.of(
                        "error", "ADMIN_OPERATION_FAILED",
                        "message", e.getMessage()
                ));
    }
}
