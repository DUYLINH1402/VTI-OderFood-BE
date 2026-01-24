package com.foodorder.backend.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO cho API thống kê tổng quan nâng cao
 * Bao gồm: AOV, Tỷ lệ hủy đơn, Khách hàng mới, Điểm thưởng đã dùng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response chứa thống kê nâng cao cho Dashboard")
public class AdvancedStatisticsResponse {

    @Schema(description = "Average Order Value - Giá trị đơn hàng trung bình (VND)", example = "85000")
    private BigDecimal aov;

    @Schema(description = "Tỷ lệ thay đổi AOV so với kỳ trước (%)", example = "5.5")
    private Double aovChangePercent;

    @Schema(description = "Tổng số đơn hàng trong kỳ", example = "500")
    private Long totalOrders;

    @Schema(description = "Số đơn hàng bị hủy", example = "15")
    private Long cancelledOrders;

    @Schema(description = "Tỷ lệ hủy đơn (%)", example = "3.0")
    private Double cancellationRate;

    @Schema(description = "Tỷ lệ thay đổi tỷ lệ hủy so với kỳ trước (%)", example = "-2.5")
    private Double cancellationRateChangePercent;

    @Schema(description = "Số khách hàng mới trong kỳ", example = "50")
    private Long newCustomers;

    @Schema(description = "Tỷ lệ thay đổi khách hàng mới so với kỳ trước (%)", example = "10.0")
    private Double newCustomersChangePercent;

    @Schema(description = "Tổng điểm thưởng đã sử dụng trong kỳ", example = "5000")
    private Long pointsUsed;

    @Schema(description = "Tỷ lệ thay đổi điểm thưởng đã dùng so với kỳ trước (%)", example = "15.0")
    private Double pointsUsedChangePercent;

    @Schema(description = "Giá trị quy đổi từ điểm thưởng (VND)", example = "50000")
    private BigDecimal pointsDiscountValue;

    @Schema(description = "Khoảng thời gian thống kê (7, 30, 90 ngày)", example = "30")
    private Integer periodDays;
}

