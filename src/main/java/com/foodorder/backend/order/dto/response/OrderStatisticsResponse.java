package com.foodorder.backend.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatisticsResponse {
    private long totalOrders;
    private long completedOrders;   // Thay đổi từ confirmedOrders
    private long cancelledOrders;
    private long pendingOrders;     // Thay đổi từ preparingOrders
    private BigDecimal totalRevenue;
    private BigDecimal averageOrderValue;

    // Các field cũ để backward compatibility (optional)
    private long confirmedOrders;
    private long preparingOrders;
    private long shippingOrders;
    private long deliveredOrders;
    private BigDecimal totalSpent;
}
