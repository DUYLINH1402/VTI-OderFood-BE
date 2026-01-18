package com.foodorder.backend.dashboard.dto.response;

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
public class AdvancedStatisticsResponse {

    /**
     * Average Order Value - Giá trị đơn hàng trung bình
     */
    private BigDecimal aov;

    /**
     * Tỷ lệ thay đổi AOV so với kỳ trước (%)
     */
    private Double aovChangePercent;

    /**
     * Tổng số đơn hàng trong kỳ
     */
    private Long totalOrders;

    /**
     * Số đơn hàng bị hủy
     */
    private Long cancelledOrders;

    /**
     * Tỷ lệ hủy đơn (%)
     */
    private Double cancellationRate;

    /**
     * Tỷ lệ thay đổi tỷ lệ hủy so với kỳ trước (%)
     */
    private Double cancellationRateChangePercent;

    /**
     * Số khách hàng mới trong kỳ
     */
    private Long newCustomers;

    /**
     * Tỷ lệ thay đổi khách hàng mới so với kỳ trước (%)
     */
    private Double newCustomersChangePercent;

    /**
     * Tổng điểm thưởng đã sử dụng trong kỳ
     */
    private Long pointsUsed;

    /**
     * Tỷ lệ thay đổi điểm thưởng đã dùng so với kỳ trước (%)
     */
    private Double pointsUsedChangePercent;

    /**
     * Giá trị quy đổi từ điểm thưởng (VND)
     */
    private BigDecimal pointsDiscountValue;

    /**
     * Khoảng thời gian thống kê (7, 30, 90 ngày)
     */
    private Integer periodDays;
}

