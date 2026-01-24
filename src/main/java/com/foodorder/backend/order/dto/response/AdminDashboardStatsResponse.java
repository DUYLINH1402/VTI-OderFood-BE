package com.foodorder.backend.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO cho thống kê chuyên sâu dành cho StatCards trên Dashboard Admin
 * Bao gồm: Doanh thu thực, Đơn bị hủy, Ghi chú mới, và các chỉ số quan trọng khác
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response chứa thống kê tổng quan cho Dashboard Admin")
public class AdminDashboardStatsResponse {

    // === THỐNG KÊ DOANH THU ===
    @Schema(description = "Doanh thu thực (chỉ tính đơn hoàn thành, đã thanh toán)", example = "15000000")
    private BigDecimal actualRevenue;

    @Schema(description = "Tổng doanh thu (tất cả đơn, kể cả chưa thanh toán)", example = "20000000")
    private BigDecimal totalRevenue;

    @Schema(description = "Doanh thu hôm nay", example = "500000")
    private BigDecimal revenueToday;

    @Schema(description = "Doanh thu tuần này", example = "3500000")
    private BigDecimal revenueThisWeek;

    @Schema(description = "Doanh thu tháng này", example = "15000000")
    private BigDecimal revenueThisMonth;

    @Schema(description = "% tăng trưởng so với kỳ trước", example = "15.5")
    private BigDecimal revenueGrowthPercent;

    // === THỐNG KÊ ĐƠN HÀNG ===
    @Schema(description = "Tổng số đơn hàng", example = "500")
    private Long totalOrders;

    @Schema(description = "Số đơn hôm nay", example = "15")
    private Long ordersToday;

    @Schema(description = "Số đơn tuần này", example = "80")
    private Long ordersThisWeek;

    @Schema(description = "Số đơn tháng này", example = "350")
    private Long ordersThisMonth;

    // === THỐNG KÊ ĐƠN THEO TRẠNG THÁI ===
    @Schema(description = "Số đơn chờ thanh toán", example = "5")
    private Long pendingOrders;

    @Schema(description = "Số đơn đã thanh toán, chờ xác nhận", example = "10")
    private Long processingOrders;

    @Schema(description = "Số đơn đã xác nhận, đang chế biến", example = "8")
    private Long confirmedOrders;

    @Schema(description = "Số đơn đang giao hàng", example = "12")
    private Long deliveringOrders;

    @Schema(description = "Số đơn hoàn thành", example = "450")
    private Long completedOrders;

    @Schema(description = "Số đơn bị hủy", example = "15")
    private Long cancelledOrders;

    @Schema(description = "Số đơn bị hủy hôm nay", example = "1")
    private Long cancelledOrdersToday;

    @Schema(description = "Số đơn bị hủy tuần này", example = "3")
    private Long cancelledOrdersThisWeek;

    @Schema(description = "Tỷ lệ hủy đơn (%)", example = "3.0")
    private Double cancellationRate;

    // === THỐNG KÊ GHI CHÚ NỘI BỘ ===
    @Schema(description = "Số đơn có ghi chú nội bộ", example = "25")
    private Long ordersWithInternalNotes;

    @Schema(description = "Số ghi chú mới hôm nay", example = "3")
    private Long newInternalNotesToday;

    @Schema(description = "Số ghi chú mới tuần này", example = "10")
    private Long newInternalNotesThisWeek;

    // === THỐNG KÊ ĐIỂM THƯỞNG ===
    @Schema(description = "Tổng điểm đã sử dụng", example = "50000")
    private Long totalPointsUsed;

    @Schema(description = "Tổng tiền giảm từ điểm", example = "500000")
    private BigDecimal totalPointsDiscount;

    // === THỐNG KÊ COUPON ===
    @Schema(description = "Số đơn sử dụng coupon", example = "120")
    private Long ordersWithCoupon;

    @Schema(description = "Tổng tiền giảm từ coupon", example = "1200000")
    private BigDecimal totalCouponDiscount;

    // === THỐNG KÊ THANH TOÁN ===
    @Schema(description = "Số đơn đã thanh toán", example = "470")
    private Long paidOrders;

    @Schema(description = "Số đơn chưa thanh toán", example = "15")
    private Long unpaidOrders;

    @Schema(description = "Số đơn đã hoàn tiền", example = "5")
    private Long refundedOrders;

    // === GIÁ TRỊ TRUNG BÌNH ===
    @Schema(description = "Giá trị đơn hàng trung bình", example = "85000")
    private BigDecimal averageOrderValue;
}
