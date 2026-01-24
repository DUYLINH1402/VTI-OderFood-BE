package com.foodorder.backend.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Response chứa thống kê tổng quan cho Dashboard Admin")
public class DashboardStatisticsResponse {

    @Schema(description = "Tổng số khách hàng (ROLE_USER)", example = "1500")
    private Long totalCustomers;

    @Schema(description = "Doanh thu tháng hiện tại (VND)", example = "15000000")
    private BigDecimal monthlyRevenue;

    @Schema(description = "Số đơn hàng trong ngày hôm nay", example = "25")
    private Long todayOrders;

    @Schema(description = "Tổng số nhân viên (ROLE_STAFF)", example = "10")
    private Long totalStaff;

    @Schema(description = "Số đơn hàng đang chờ xử lý (PENDING + PROCESSING)", example = "5")
    private Long pendingOrders;

    @Schema(description = "Số đơn hàng đã hoàn thành hôm nay", example = "20")
    private Long completedTodayOrders;

    @Schema(description = "Tỷ lệ tăng trưởng doanh thu so với tháng trước (%)", example = "15.5")
    private Double revenueGrowthPercent;

    @Schema(description = "Tỷ lệ tăng trưởng khách hàng so với tháng trước (%)", example = "8.2")
    private Double customerGrowthPercent;
}

