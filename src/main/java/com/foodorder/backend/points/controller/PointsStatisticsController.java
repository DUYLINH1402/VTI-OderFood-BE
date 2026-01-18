package com.foodorder.backend.points.controller;

import com.foodorder.backend.points.dto.response.*;
import com.foodorder.backend.points.service.PointsStatisticsService;
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
 * Controller cho API thống kê và quản lý điểm thưởng (Admin)
 * Cung cấp các endpoint cho admin dashboard để quản lý hiệu quả chương trình điểm thưởng
 */
@RestController
@RequestMapping("/api/v1/admin/promotions/points")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class PointsStatisticsController {

    private final PointsStatisticsService pointsStatisticsService;

    // ============ THỐNG KÊ TỔNG QUAN ============

    /**
     * API lấy thống kê tổng quan về điểm thưởng
     * GET /api/v1/admin/promotions/points/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<PointsStatisticsResponse> getOverallStatistics() {
        PointsStatisticsResponse statistics = pointsStatisticsService.getOverallStatistics();
        return ResponseEntity.ok(statistics);
    }

    // ============ PHÂN TÍCH XU HƯỚNG ============

    /**
     * API phân tích xu hướng điểm thưởng trong khoảng thời gian
     * GET /api/v1/admin/promotions/points/analytics?startDate=...&endDate=...
     */
    @GetMapping("/analytics")
    public ResponseEntity<PointsTrendAnalyticsResponse> getTrendAnalytics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        PointsTrendAnalyticsResponse analytics = pointsStatisticsService.getTrendAnalytics(startDate, endDate);
        return ResponseEntity.ok(analytics);
    }

    /**
     * API lấy xu hướng điểm theo ngày
     * GET /api/v1/admin/promotions/points/trend?startDate=...&endDate=...
     */
    @GetMapping("/trend")
    public ResponseEntity<List<PointsTrendAnalyticsResponse.DailyPointsData>> getDailyTrend(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<PointsTrendAnalyticsResponse.DailyPointsData> trend = pointsStatisticsService.getDailyTrend(startDate, endDate);
        return ResponseEntity.ok(trend);
    }

    // ============ BÁO CÁO THEO USER ============

    /**
     * API lấy chi tiết điểm thưởng của một user
     * GET /api/v1/admin/promotions/points/users/{userId}
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserPointsDetailResponse> getUserPointsDetail(@PathVariable Long userId) {
        UserPointsDetailResponse userDetail = pointsStatisticsService.getUserPointsDetail(userId);
        return ResponseEntity.ok(userDetail);
    }

    /**
     * API lấy top user có điểm thưởng cao nhất
     * GET /api/v1/admin/promotions/points/users/top?limit=10
     */
    @GetMapping("/users/top")
    public ResponseEntity<List<TopUserByPointsResponse>> getTopUsersByPoints(
            @RequestParam(defaultValue = "10") int limit) {
        List<TopUserByPointsResponse> topUsers = pointsStatisticsService.getTopUsersByPoints(limit);
        return ResponseEntity.ok(topUsers);
    }

    /**
     * API lấy danh sách user có điểm với phân trang và filter
     * GET /api/v1/admin/promotions/points/users?minBalance=100
     */
    @GetMapping("/users")
    public ResponseEntity<Page<UserPointsDetailResponse>> getUsersWithPoints(
            @RequestParam(defaultValue = "0") int minBalance,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserPointsDetailResponse> users = pointsStatisticsService.getUsersWithPoints(minBalance, pageable);
        return ResponseEntity.ok(users);
    }

    // ============ QUẢN LÝ ĐIỂM (ADMIN) ============

    /**
     * API điều chỉnh điểm cho user
     * POST /api/v1/admin/promotions/points/users/{userId}/adjust
     */
    @PostMapping("/users/{userId}/adjust")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> adjustUserPoints(
            @PathVariable Long userId,
            @RequestBody AdjustPointsRequest request) {
        pointsStatisticsService.adjustUserPoints(userId, request.getAmount(), request.getReason());

        Map<String, Object> response = Map.of(
                "success", true,
                "message", "Đã điều chỉnh điểm thành công",
                "userId", userId,
                "adjustedAmount", request.getAmount()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * API cộng điểm hàng loạt cho nhiều user
     * POST /api/v1/admin/promotions/points/bulk-add
     */
    @PostMapping("/bulk-add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> bulkAddPoints(@RequestBody BulkAddPointsRequest request) {
        pointsStatisticsService.bulkAddPoints(request.getUserIds(), request.getAmount(), request.getReason());

        Map<String, Object> response = Map.of(
                "success", true,
                "message", "Đã cộng điểm hàng loạt thành công",
                "totalUsers", request.getUserIds().size(),
                "pointsPerUser", request.getAmount()
        );
        return ResponseEntity.ok(response);
    }

    // ============ DASHBOARD TỔNG HỢP ============

    /**
     * API lấy dashboard tổng hợp cho quản lý điểm thưởng
     * GET /api/v1/admin/promotions/points/dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getPointsDashboard() {

        // Lấy thống kê tổng quan
        PointsStatisticsResponse statistics = pointsStatisticsService.getOverallStatistics();

        // Lấy top users
        List<TopUserByPointsResponse> topUsers = pointsStatisticsService.getTopUsersByPoints(10);

        // Lấy xu hướng 30 ngày gần đây
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(30);
        PointsTrendAnalyticsResponse trendAnalytics = pointsStatisticsService.getTrendAnalytics(startDate, endDate);

        Map<String, Object> dashboard = Map.of(
                "statistics", statistics,
                "topUsersByPoints", topUsers,
                "trendAnalytics30Days", trendAnalytics
        );

        return ResponseEntity.ok(dashboard);
    }

    // ============ DTO CLASSES ============

    @lombok.Data
    public static class AdjustPointsRequest {
        private int amount;
        private String reason;
    }

    @lombok.Data
    public static class BulkAddPointsRequest {
        private List<Long> userIds;
        private int amount;
        private String reason;
    }
}

