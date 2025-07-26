package com.foodorder.backend.order.service;

import com.foodorder.backend.order.dto.request.OrderRequest;
import com.foodorder.backend.order.dto.request.UpdateOrderStatusRequest;
import com.foodorder.backend.order.dto.response.OrderResponse;
import com.foodorder.backend.order.dto.response.PageResponse;
import com.foodorder.backend.order.dto.response.OrderStatisticsResponse;
import org.springframework.data.domain.PageRequest;

public interface OrderService {
    OrderResponse createOrder(OrderRequest orderRequest);

    PageResponse<OrderResponse> getOrdersByUser(Long userId, String status, PageRequest pageRequest);

    OrderResponse getOrderDetail(String orderCode, Long userId);

    void updateOrderStatus(String orderCode, Long userId, UpdateOrderStatusRequest request);

    void cancelOrder(String orderCode, Long userId, String cancelReason);

    OrderStatisticsResponse getOrderStatistics(Long userId);
}
