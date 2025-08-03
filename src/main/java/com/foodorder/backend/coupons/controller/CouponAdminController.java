package com.foodorder.backend.coupons.controller;

import com.foodorder.backend.coupons.dto.response.CouponResponse;
import com.foodorder.backend.coupons.entity.CouponStatus;
import com.foodorder.backend.coupons.service.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Admin Controller cho quản lý Coupon nâng cao
 * Các chức năng dành riêng cho admin/manager
 */
@RestController
@RequestMapping("/api/v1/admin/coupons")
@Slf4j
public class CouponAdminController {

    @Autowired
    private CouponService couponService;

    // === DASHBOARD & ANALYTICS ===

    /**
     * Dashboard tổng quan coupon
     * GET /api/v1/admin/coupons/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getCouponDashboard() {
        log.info("Admin requested coupon dashboard");

        var statistics = couponService.getCouponStatistics();
        var mostUsedCoupons = couponService.getMostUsedCoupons(5);
        var expiringSoon = couponService.getExpiringSoonCoupons(7);

        Map<String, Object> dashboard = Map.of(
            "statistics", statistics,
            "mostUsedCoupons", mostUsedCoupons,
            "expiringSoonCoupons", expiringSoon,
            "totalActiveCoupons", statistics.getOrDefault(CouponStatus.ACTIVE, 0L),
            "totalExpiredCoupons", statistics.getOrDefault(CouponStatus.EXPIRED, 0L)
        );

        return ResponseEntity.ok(dashboard);
    }

    /**
     * Phân tích hiệu quả coupon
     * GET /api/v1/admin/coupons/analytics
     */
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getCouponAnalytics() {
        log.info("Admin requested coupon analytics");

        var topCoupons = couponService.getMostUsedCoupons(20);
        var statistics = couponService.getCouponStatistics();

        // Tính toán các metrics
        long totalCoupons = statistics.values().stream().mapToLong(Long::longValue).sum();
        long activeCoupons = statistics.getOrDefault(CouponStatus.ACTIVE, 0L);
        double activeRate = totalCoupons > 0 ? (double) activeCoupons / totalCoupons * 100 : 0;

        Map<String, Object> analytics = Map.of(
            "totalCoupons", totalCoupons,
            "activeCoupons", activeCoupons,
            "activeRate", Math.round(activeRate * 100.0) / 100.0,
            "topPerformingCoupons", topCoupons,
            "statusDistribution", statistics
        );

        return ResponseEntity.ok(analytics);
    }

    // === BULK OPERATIONS ===

    /**
     * Kích hoạt hàng loạt coupon
     * PUT /api/v1/admin/coupons/bulk-activate
     */
    @PutMapping("/bulk-activate")
    public ResponseEntity<Map<String, Object>> bulkActivateCoupons(@RequestBody List<Long> couponIds) {
        log.info("Admin requested bulk activate {} coupons", couponIds.size());

        int successCount = 0;
        int failCount = 0;

        for (Long couponId : couponIds) {
            try {
                couponService.activateCoupon(couponId);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to activate coupon {}: {}", couponId, e.getMessage());
                failCount++;
            }
        }

        Map<String, Object> result = Map.of(
            "totalRequested", couponIds.size(),
            "successCount", successCount,
            "failCount", failCount
        );

        return ResponseEntity.ok(result);
    }

    /**
     * Vô hiệu hóa hàng loạt coupon
     * PUT /api/v1/admin/coupons/bulk-deactivate
     */
    @PutMapping("/bulk-deactivate")
    public ResponseEntity<Map<String, Object>> bulkDeactivateCoupons(@RequestBody List<Long> couponIds) {
        log.info("Admin requested bulk deactivate {} coupons", couponIds.size());

        int successCount = 0;
        int failCount = 0;

        for (Long couponId : couponIds) {
            try {
                couponService.deactivateCoupon(couponId);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to deactivate coupon {}: {}", couponId, e.getMessage());
                failCount++;
            }
        }

        Map<String, Object> result = Map.of(
            "totalRequested", couponIds.size(),
            "successCount", successCount,
            "failCount", failCount
        );

        return ResponseEntity.ok(result);
    }

    /**
     * Xóa hàng loạt coupon
     * DELETE /api/v1/admin/coupons/bulk-delete
     */
    @DeleteMapping("/bulk-delete")
    public ResponseEntity<Map<String, Object>> bulkDeleteCoupons(@RequestBody List<Long> couponIds) {
        log.info("Admin requested bulk delete {} coupons", couponIds.size());

        int successCount = 0;
        int failCount = 0;

        for (Long couponId : couponIds) {
            try {
                couponService.deleteCoupon(couponId);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to delete coupon {}: {}", couponId, e.getMessage());
                failCount++;
            }
        }

        Map<String, Object> result = Map.of(
            "totalRequested", couponIds.size(),
            "successCount", successCount,
            "failCount", failCount
        );

        return ResponseEntity.ok(result);
    }

    // === CAMPAIGN MANAGEMENT ===

    /**
     * Tạo campaign coupon sinh nhật hàng loạt
     * POST /api/v1/admin/coupons/birthday-campaign
     */
    @PostMapping("/birthday-campaign")
    public ResponseEntity<Map<String, Object>> createBirthdayCampaign(@RequestBody List<Long> userIds) {
        log.info("Admin requested birthday campaign for {} users", userIds.size());

        int successCount = 0;
        int failCount = 0;

        for (Long userId : userIds) {
            try {
                couponService.createBirthdayCouponForUser(userId);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to create birthday coupon for user {}: {}", userId, e.getMessage());
                failCount++;
            }
        }

        Map<String, Object> result = Map.of(
            "totalUsers", userIds.size(),
            "successCount", successCount,
            "failCount", failCount,
            "campaignType", "BIRTHDAY"
        );

        return ResponseEntity.ok(result);
    }

    /**
     * Tạo campaign welcome coupon hàng loạt
     * POST /api/v1/admin/coupons/welcome-campaign
     */
    @PostMapping("/welcome-campaign")
    public ResponseEntity<Map<String, Object>> createWelcomeCampaign(@RequestBody List<Long> userIds) {
        log.info("Admin requested welcome campaign for {} users", userIds.size());

        int successCount = 0;
        int failCount = 0;

        for (Long userId : userIds) {
            try {
                couponService.createFirstOrderCouponForUser(userId);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to create welcome coupon for user {}: {}", userId, e.getMessage());
                failCount++;
            }
        }

        Map<String, Object> result = Map.of(
            "totalUsers", userIds.size(),
            "successCount", successCount,
            "failCount", failCount,
            "campaignType", "WELCOME"
        );

        return ResponseEntity.ok(result);
    }

    // === SYSTEM MAINTENANCE ===

    /**
     * Chạy tất cả scheduled tasks ngay lập tức (manual trigger)
     * POST /api/v1/admin/coupons/run-maintenance
     */
    @PostMapping("/run-maintenance")
    public ResponseEntity<Map<String, String>> runMaintenanceTasks() {
        log.info("Admin triggered manual maintenance tasks");

        try {
            // Update expired coupons
            couponService.updateExpiredCoupons();
            log.info("Updated expired coupons");

            // Update used out coupons
            couponService.updateUsedOutCoupons();
            log.info("Updated used out coupons");

            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "All maintenance tasks completed successfully"
            ));

        } catch (Exception e) {
            log.error("Error running maintenance tasks: {}", e.getMessage(), e);
            return ResponseEntity.ok(Map.of(
                "status", "ERROR",
                "message", "Some maintenance tasks failed: " + e.getMessage()
            ));
        }
    }

    // === ADVANCED REPORTS ===

    /**
     * Báo cáo chi tiết theo khoảng thời gian
     * GET /api/v1/admin/coupons/detailed-report?startDate=2024-01-01&endDate=2024-01-31
     */
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

    // === EXCEPTION HANDLING ===

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
