package com.foodorder.backend.order.dto.response;

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
public class AdminDashboardStatsResponse {

    // === THỐNG KÊ DOANH THU ===
    private BigDecimal actualRevenue;           // Doanh thu thực (chỉ tính đơn hoàn thành, đã thanh toán)
    private BigDecimal totalRevenue;            // Tổng doanh thu (tất cả đơn, kể cả chưa thanh toán)
    private BigDecimal revenueToday;            // Doanh thu hôm nay
    private BigDecimal revenueThisWeek;         // Doanh thu tuần này
    private BigDecimal revenueThisMonth;        // Doanh thu tháng này
    private BigDecimal revenueGrowthPercent;    // % tăng trưởng so với kỳ trước

    // === THỐNG KÊ ĐƠN HÀNG ===
    private Long totalOrders;                   // Tổng số đơn hàng
    private Long ordersToday;                   // Số đơn hôm nay
    private Long ordersThisWeek;                // Số đơn tuần này
    private Long ordersThisMonth;               // Số đơn tháng này

    // === THỐNG KÊ ĐƠN THEO TRẠNG THÁI ===
    private Long pendingOrders;                 // Đơn chờ thanh toán
    private Long processingOrders;              // Đơn đã thanh toán, chờ xác nhận
    private Long confirmedOrders;               // Đơn đã xác nhận, đang chế biến
    private Long deliveringOrders;              // Đơn đang giao hàng
    private Long completedOrders;               // Đơn hoàn thành
    private Long cancelledOrders;               // Đơn bị hủy
    private Long cancelledOrdersToday;          // Đơn bị hủy hôm nay
    private Long cancelledOrdersThisWeek;       // Đơn bị hủy tuần này
    private Double cancellationRate;            // Tỷ lệ hủy đơn (%)

    // === THỐNG KÊ GHI CHÚ NỘI BỘ ===
    private Long ordersWithInternalNotes;       // Số đơn có ghi chú nội bộ
    private Long newInternalNotesToday;         // Số ghi chú mới hôm nay
    private Long newInternalNotesThisWeek;      // Số ghi chú mới tuần này

    // === THỐNG KÊ ĐIỂM THƯỞNG ===
    private Long totalPointsUsed;               // Tổng điểm đã sử dụng
    private BigDecimal totalPointsDiscount;     // Tổng tiền giảm từ điểm

    // === THỐNG KÊ COUPON ===
    private Long ordersWithCoupon;              // Số đơn sử dụng coupon
    private BigDecimal totalCouponDiscount;     // Tổng tiền giảm từ coupon

    // === THỐNG KÊ THANH TOÁN ===
    private Long paidOrders;                    // Số đơn đã thanh toán
    private Long unpaidOrders;                  // Số đơn chưa thanh toán
    private Long refundedOrders;                // Số đơn đã hoàn tiền

    // === GIÁ TRỊ TRUNG BÌNH ===
    private BigDecimal averageOrderValue;       // Giá trị đơn hàng trung bình
}
