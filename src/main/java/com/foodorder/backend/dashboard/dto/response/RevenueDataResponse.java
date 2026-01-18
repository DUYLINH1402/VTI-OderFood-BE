package com.foodorder.backend.dashboard.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO trả về dữ liệu doanh thu theo ngày
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueDataResponse {

    // Danh sách dữ liệu doanh thu theo từng ngày
    private List<DailyRevenue> dailyRevenues;

    // Tổng doanh thu trong khoảng thời gian
    private BigDecimal totalRevenue;

    // Tổng số đơn hàng trong khoảng thời gian
    private Long totalOrders;

    // Số ngày thống kê
    private Integer days;

    /**
     * DTO con chứa doanh thu của 1 ngày
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyRevenue {
        // Ngày
        private LocalDate date;

        // Doanh thu ngày đó
        private BigDecimal revenue;

        // Số đơn hàng ngày đó
        private Long orderCount;
    }
}
