package com.foodorder.backend.dashboard.service;

import com.foodorder.backend.dashboard.dto.response.*;

/**
 * Service interface cho Dashboard Admin
 * Cung cấp các phương thức thống kê và lấy dữ liệu cho dashboard
 */
public interface DashboardService {

    /**
     * Lấy thống kê tổng quan cho dashboard
     * Bao gồm: tổng khách hàng, doanh thu tháng, đơn hàng hôm nay, số nhân viên
     *
     * @return DashboardStatisticsResponse chứa các thống kê tổng quan
     */
    DashboardStatisticsResponse getStatistics();

    /**
     * Lấy dữ liệu doanh thu theo số ngày
     *
     * @param days số ngày cần lấy dữ liệu (mặc định 7 ngày)
     * @return RevenueDataResponse chứa doanh thu theo từng ngày
     */
    RevenueDataResponse getRevenueData(int days);

    /**
     * Lấy danh sách hoạt động gần đây
     * Bao gồm: đơn hàng mới, đơn hoàn thành, đơn bị hủy, khách hàng mới đăng ký
     *
     * @param limit số lượng hoạt động cần lấy (mặc định 10)
     * @return RecentActivityResponse chứa danh sách hoạt động
     */
    RecentActivityResponse getRecentActivities(int limit);

    // ============ ADVANCED STATISTICS ============

    /**
     * Lấy top 5 món ăn bán chạy nhất
     *
     * @param periodDays khoảng thời gian thống kê (7, 30, 90 ngày)
     * @return TopSellingFoodResponse chứa danh sách top món bán chạy
     */
    TopSellingFoodResponse getTopSellingFoods(int periodDays);

    /**
     * Lấy thống kê nâng cao: AOV, tỷ lệ hủy đơn, khách hàng mới, điểm thưởng đã dùng
     *
     * @param periodDays khoảng thời gian thống kê (7, 30, 90 ngày)
     * @return AdvancedStatisticsResponse chứa các chỉ số thống kê nâng cao
     */
    AdvancedStatisticsResponse getAdvancedStatistics(int periodDays);

    /**
     * Lấy cơ cấu doanh thu theo nhóm món (3 nhóm chính + 1 nhóm "Khác")
     *
     * @param periodDays khoảng thời gian thống kê (7, 30, 90 ngày)
     * @return RevenueByCategoryResponse chứa cơ cấu doanh thu
     */
    RevenueByCategoryResponse getRevenueByCategory(int periodDays);

    /**
     * Lấy chi tiết hiệu quả từng món ăn
     *
     * @param periodDays khoảng thời gian thống kê (7, 30, 90 ngày)
     * @param page số trang (bắt đầu từ 0)
     * @param size số món mỗi trang
     * @return FoodPerformanceResponse chứa chi tiết hiệu quả món ăn
     */
    FoodPerformanceResponse getFoodPerformance(int periodDays, int page, int size);
}
