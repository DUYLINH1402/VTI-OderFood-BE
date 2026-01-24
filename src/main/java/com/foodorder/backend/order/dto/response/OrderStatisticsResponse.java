package com.foodorder.backend.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response chứa thống kê đơn hàng")
public class OrderStatisticsResponse {

    @Schema(description = "Tổng số đơn hàng", example = "500")
    private long totalOrders;

    @Schema(description = "Số đơn hoàn thành", example = "450")
    private long completedOrders;

    @Schema(description = "Số đơn bị hủy", example = "15")
    private long cancelledOrders;

    @Schema(description = "Số đơn đang chờ xử lý", example = "20")
    private long pendingOrders;

    @Schema(description = "Tổng doanh thu (VND)", example = "15000000")
    private BigDecimal totalRevenue;

    @Schema(description = "Giá trị đơn hàng trung bình (VND)", example = "85000")
    private BigDecimal averageOrderValue;

    // Các field cũ để backward compatibility (optional)
    @Schema(description = "Số đơn đã xác nhận (deprecated)", example = "10")
    private long confirmedOrders;

    @Schema(description = "Số đơn đang chế biến (deprecated)", example = "5")
    private long preparingOrders;

    @Schema(description = "Số đơn đang giao (deprecated)", example = "8")
    private long shippingOrders;

    @Schema(description = "Số đơn đã giao (deprecated)", example = "450")
    private long deliveredOrders;

    @Schema(description = "Tổng chi tiêu (deprecated)", example = "15000000")
    private BigDecimal totalSpent;
}
