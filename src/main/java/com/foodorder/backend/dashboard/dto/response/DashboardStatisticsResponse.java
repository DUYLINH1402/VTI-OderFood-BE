package com.foodorder.backend.dashboard.dto.response;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO trả về thống kê tổng quan cho Dashboard Admin
 * Bao gồm: tổng khách hàng, doanh thu tháng, đơn hàng hôm nay, số nhân viên
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatisticsResponse {

    // Tổng số khách hàng (ROLE_USER)
    private Long totalCustomers;

    // Doanh thu tháng hiện tại
    private BigDecimal monthlyRevenue;

    // Số đơn hàng trong ngày hôm nay
    private Long todayOrders;

    // Tổng số nhân viên (ROLE_STAFF)
    private Long totalStaff;

    // Số đơn hàng đang chờ xử lý (PENDING + PROCESSING)
    private Long pendingOrders;

    // Số đơn hàng đã hoàn thành hôm nay
    private Long completedTodayOrders;

    // Tỷ lệ tăng trưởng doanh thu so với tháng trước (%)
    private Double revenueGrowthPercent;

    // Tỷ lệ tăng trưởng khách hàng so với tháng trước (%)
    private Double customerGrowthPercent;
}

