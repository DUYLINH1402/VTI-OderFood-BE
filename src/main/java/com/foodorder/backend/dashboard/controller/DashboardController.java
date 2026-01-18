package com.foodorder.backend.dashboard.controller;

import com.foodorder.backend.dashboard.dto.response.*;
import com.foodorder.backend.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các API Dashboard cho Admin
 * Cung cấp các endpoint thống kê và báo cáo tổng quan
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
@Tag(name = "Dashboard", description = "API thống kê Dashboard - Admin/Staff")
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Thống kê tổng quan", description = "Lấy thống kê tổng quan: tổng khách hàng, doanh thu tháng, đơn hàng hôm nay, số nhân viên.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    @GetMapping("/statistics")
    public ResponseEntity<DashboardStatisticsResponse> getStatistics() {
        DashboardStatisticsResponse statistics = dashboardService.getStatistics();
        return ResponseEntity.ok(statistics);
    }

    @Operation(summary = "Doanh thu theo ngày", description = "Lấy dữ liệu doanh thu theo số ngày (mặc định 7 ngày, tối đa 365 ngày).")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/revenue")
    public ResponseEntity<RevenueDataResponse> getRevenueData(
            @Parameter(description = "Số ngày cần lấy dữ liệu") @RequestParam(defaultValue = "7") int days) {
        RevenueDataResponse revenueData = dashboardService.getRevenueData(days);
        return ResponseEntity.ok(revenueData);
    }

    @Operation(summary = "Hoạt động gần đây", description = "Lấy danh sách hoạt động gần đây: đơn hàng mới, đơn hoàn thành, đơn bị hủy, khách hàng mới.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/activities")
    public ResponseEntity<RecentActivityResponse> getRecentActivities(
            @Parameter(description = "Số lượng hoạt động cần lấy") @RequestParam(defaultValue = "10") int limit) {
        RecentActivityResponse activities = dashboardService.getRecentActivities(limit);
        return ResponseEntity.ok(activities);
    }

    // ============ ADVANCED STATISTICS ENDPOINTS ============

    @Operation(summary = "Top món bán chạy", description = "Lấy top 5 món ăn bán chạy nhất theo khoảng thời gian.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/top-selling-foods")
    public ResponseEntity<TopSellingFoodResponse> getTopSellingFoods(
            @Parameter(description = "Khoảng thời gian: 7 (tuần), 30 (tháng), 90 (quý)") @RequestParam(defaultValue = "7") int period) {
        TopSellingFoodResponse response = dashboardService.getTopSellingFoods(period);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Thống kê nâng cao", description = "Lấy thống kê nâng cao: AOV, tỷ lệ hủy đơn, khách hàng mới, điểm thưởng đã dùng.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/advanced-statistics")
    public ResponseEntity<AdvancedStatisticsResponse> getAdvancedStatistics(
            @Parameter(description = "Khoảng thời gian: 7 (tuần), 30 (tháng), 90 (quý)") @RequestParam(defaultValue = "7") int period) {
        AdvancedStatisticsResponse response = dashboardService.getAdvancedStatistics(period);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Doanh thu theo danh mục", description = "Lấy cơ cấu doanh thu theo nhóm món (3 nhóm chính + 1 nhóm 'Khác').")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/revenue-by-category")
    public ResponseEntity<RevenueByCategoryResponse> getRevenueByCategory(
            @Parameter(description = "Khoảng thời gian: 7 (tuần), 30 (tháng), 90 (quý)") @RequestParam(defaultValue = "7") int period) {
        RevenueByCategoryResponse response = dashboardService.getRevenueByCategory(period);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Hiệu quả món ăn", description = "Lấy chi tiết hiệu quả từng món ăn: Số đơn, Doanh thu, Đánh giá, Xu hướng.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/food-performance")
    public ResponseEntity<FoodPerformanceResponse> getFoodPerformance(
            @Parameter(description = "Khoảng thời gian: 7 (tuần), 30 (tháng), 90 (quý)") @RequestParam(defaultValue = "7") int period,
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số món mỗi trang") @RequestParam(defaultValue = "10") int size) {
        FoodPerformanceResponse response = dashboardService.getFoodPerformance(period, page, size);
        return ResponseEntity.ok(response);
    }
}
