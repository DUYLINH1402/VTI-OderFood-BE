package com.foodorder.backend.order.service;

import com.foodorder.backend.order.dto.request.UpdateOrderStatusRequest;
import com.foodorder.backend.order.dto.response.OrderResponse;
import com.foodorder.backend.order.dto.response.OrderStatisticsResponse;
import com.foodorder.backend.order.dto.response.PageResponse;
import com.foodorder.backend.order.entity.Order;
import com.foodorder.backend.order.entity.OrderStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.Set;

/**
 * Core Service chứa logic nghiệp vụ chung cho quản lý đơn hàng
 * Được sử dụng bởi các Role-specific Services
 */
public interface OrderCoreService {

    /**
     * Tìm đơn hàng theo ID với validation
     */
    Order findOrderByIdWithValidation(Long orderId);

    /**
     * Tìm đơn hàng theo mã đơn hàng
     */
    Order findOrderByCode(String orderCode);

    /**
     * Lấy danh sách đơn hàng với specification
     */
    PageResponse<OrderResponse> getOrdersWithSpecification(
            Specification<Order> spec, PageRequest pageRequest);

    /**
     * Tạo specification cho filter đơn hàng
     */
    Specification<Order> createOrderSpecification(
            String status, String orderCode, String customerName,
            String startDate, String endDate, Long staffId);

    /**
     * Cập nhật trạng thái đơn hàng với validation
     */
    Order updateOrderStatusWithValidation(
            Long orderId, UpdateOrderStatusRequest request,
            Set<OrderStatus> allowedStatuses);

    /**
     * Lấy thống kê đơn hàng
     */
    OrderStatisticsResponse calculateOrderStatistics(
            String startDate, String endDate, String period);

    /**
     * Validate trạng thái transition
     */
    boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus, String role);

    /**
     * Lấy danh sách trạng thái được phép theo role
     */
    Set<OrderStatus> getAllowedStatusesForRole(String role);

    /**
     * Gửi notification khi cập nhật trạng thái
     */
    void sendOrderStatusNotification(Order order, OrderStatus oldStatus);
}
