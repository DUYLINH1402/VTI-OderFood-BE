package com.foodorder.backend.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Response chứa dữ liệu doanh thu theo ngày")
public class RevenueDataResponse {

    @Schema(description = "Danh sách dữ liệu doanh thu theo từng ngày")
    private List<DailyRevenue> dailyRevenues;

    @Schema(description = "Tổng doanh thu trong khoảng thời gian (VND)", example = "5000000")
    private BigDecimal totalRevenue;

    @Schema(description = "Tổng số đơn hàng trong khoảng thời gian", example = "150")
    private Long totalOrders;

    @Schema(description = "Số ngày thống kê", example = "7")
    private Integer days;

    /**
     * DTO con chứa doanh thu của 1 ngày
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Doanh thu của 1 ngày")
    public static class DailyRevenue {
        @Schema(description = "Ngày", example = "2025-01-20")
        private LocalDate date;

        @Schema(description = "Doanh thu ngày đó (VND)", example = "500000")
        private BigDecimal revenue;

        @Schema(description = "Số đơn hàng ngày đó", example = "15")
        private Long orderCount;
    }
}
