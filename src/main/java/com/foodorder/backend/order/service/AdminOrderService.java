package com.foodorder.backend.order.service;

import com.foodorder.backend.order.dto.request.UpdateOrderStatusRequest;
import com.foodorder.backend.order.dto.response.OrderResponse;
import com.foodorder.backend.order.dto.response.OrderStatisticsResponse;
import com.foodorder.backend.order.dto.response.PageResponse;
import org.springframework.data.domain.PageRequest;

/**
 * Service dành riêng cho ADMIN quản lý đơn hàng
 * Tập trung vào quản lý tổng thể, thống kê và giám sát
 */
public interface AdminOrderService {

    /**
     * Lấy tất cả đơn hàng với bộ lọc đa dạng (ADMIN)
     */
    PageResponse<OrderResponse> getAllOrdersWithFilters(
            String status, String orderCode, String customerName,
            String startDate, String endDate, Long staffId, PageRequest pageRequest);

    /**
     * Thống kê đơn hàng tổng quan (ADMIN)
     */
    OrderStatisticsResponse getOrderStatistics(String startDate, String endDate, String period);

    /**
     * Cập nhật trạng thái đơn hàng với quyền cao nhất (ADMIN)
     */
    OrderResponse updateOrderStatusWithFullAccess(Long orderId, UpdateOrderStatusRequest request);

    /**
     * Xóa đơn hàng (ADMIN only)
     */
    void deleteOrder(Long orderId);

    /**
     * Khôi phục đơn hàng đã hủy (ADMIN)
     */
    OrderResponse restoreOrder(Long orderId);

    /**
     * Lấy chi tiết đơn hàng với thông tin đầy đủ (ADMIN)
     */
    OrderResponse getOrderFullDetails(Long orderId);

    /**
     * Tìm kiếm đơn hàng nâng cao (ADMIN)
     */
    PageResponse<OrderResponse> advancedSearch(
            String keyword, String status, String customerEmail, String customerPhone,
            Double minAmount, Double maxAmount, PageRequest pageRequest);
}
