package com.foodorder.backend.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatisticsResponse {
    private long totalOrders;
    private long confirmedOrders;
    private long preparingOrders;
    private long shippingOrders;
    private long deliveredOrders;
    private long cancelledOrders;
    private double totalSpent;
    private double averageOrderValue;
}
