package com.foodorder.backend.order.service.impl;

import com.foodorder.backend.order.dto.request.UpdateOrderStatusRequest;
import com.foodorder.backend.order.dto.response.OrderResponse;
import com.foodorder.backend.order.dto.response.OrderStatisticsResponse;
import com.foodorder.backend.order.dto.response.PageResponse;
import com.foodorder.backend.order.entity.Order;
import com.foodorder.backend.order.entity.OrderStatus;
import com.foodorder.backend.order.service.AdminOrderService;
import com.foodorder.backend.order.service.OrderCoreService;
import com.foodorder.backend.order.util.OrderMapper;
import com.foodorder.backend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Admin Order Service Implementation
 * Sử dụng OrderCoreService cho logic chung và thêm logic riêng cho Admin
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminOrderServiceImpl implements AdminOrderService {

    private final OrderCoreService orderCoreService;
    private final OrderMapper orderMapper;

    @Override
    public PageResponse<OrderResponse> getAllOrdersWithFilters(
            String status, String orderCode, String customerName,
            String startDate, String endDate, Long staffId, PageRequest pageRequest) {

        log.info("Admin getting all orders with filters - status: {}", status);

        Specification<Order> spec = orderCoreService.createOrderSpecification(
            status, orderCode, customerName, startDate, endDate, staffId);

        return orderCoreService.getOrdersWithSpecification(spec, pageRequest);
    }

    @Override
    public OrderStatisticsResponse getOrderStatistics(String startDate, String endDate, String period) {
        log.info("Admin getting order statistics - period: {}", period);

        return orderCoreService.calculateOrderStatistics(startDate, endDate, period);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatusWithFullAccess(Long orderId, UpdateOrderStatusRequest request) {
        log.info("Admin updating order {} to status {}", orderId, request.getStatus());

        // Admin có quyền cập nhật tất cả trạng thái
        Set<OrderStatus> allowedStatuses = orderCoreService.getAllowedStatusesForRole("ROLE_ROLE_ADMIN");

        Order updatedOrder = orderCoreService.updateOrderStatusWithValidation(
            orderId, request, allowedStatuses);

        return orderMapper.toOrderResponse(updatedOrder);
    }

    @Override
    @Transactional
    public void deleteOrder(Long orderId) {
        log.warn("Admin deleting order {}", orderId);

        Order order = orderCoreService.findOrderByIdWithValidation(orderId);

        // Soft delete - chuyển trạng thái thành CANCELLED thay vì xóa thật
        if (order.getStatus() != OrderStatus.CANCELLED) {
            UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
            request.setStatus("CANCELLED");
            request.setNote("Đã xóa bởi Admin");

            Set<OrderStatus> allowedStatuses = orderCoreService.getAllowedStatusesForRole("ROLE_ROLE_ADMIN");
            orderCoreService.updateOrderStatusWithValidation(orderId, request, allowedStatuses);
        }
    }

    @Override
    @Transactional
    public OrderResponse restoreOrder(Long orderId) {
        log.info("Admin restoring order {}", orderId);

        Order order = orderCoreService.findOrderByIdWithValidation(orderId);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
            request.setStatus("PENDING");
            request.setNote("Khôi phục bởi Admin");

            Set<OrderStatus> allowedStatuses = orderCoreService.getAllowedStatusesForRole("ROLE_ROLE_ADMIN");
            Order restoredOrder = orderCoreService.updateOrderStatusWithValidation(orderId, request, allowedStatuses);

            return orderMapper.toOrderResponse(restoredOrder);
        }

        throw new ResourceNotFoundException("ORDER_NOT_CANCELLED", "Chỉ có thể khôi phục đơn hàng đã hủy");
    }

    @Override
    public OrderResponse getOrderFullDetails(Long orderId) {
        log.info("Admin getting full order details: {}", orderId);

        Order order = orderCoreService.findOrderByIdWithValidation(orderId);
        return orderMapper.toOrderResponse(order);
    }

    @Override
    public PageResponse<OrderResponse> advancedSearch(
            String keyword, String status, String customerEmail, String customerPhone,
            Double minAmount, Double maxAmount, PageRequest pageRequest) {

        log.info("Admin performing advanced search with keyword: {}", keyword);

        // Tạo specification phức tạp cho tìm kiếm nâng cao
        Specification<Order> spec = Specification.where(null);

        // Tìm kiếm theo keyword (order code hoặc customer name)
        if (keyword != null && !keyword.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("orderCode")), "%" + keyword.toLowerCase() + "%"),
                cb.like(cb.lower(root.get("user").get("fullName")), "%" + keyword.toLowerCase() + "%")
            ));
        }

        // Filter theo status
        if (status != null && !"all".equals(status)) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), orderStatus));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid order status in advanced search: {}", status);
            }
        }

        // Filter theo email khách hàng
        if (customerEmail != null && !customerEmail.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                cb.like(cb.lower(root.get("user").get("email")), "%" + customerEmail.toLowerCase() + "%"));
        }

        // Filter theo số điện thoại khách hàng
        if (customerPhone != null && !customerPhone.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                cb.like(root.get("user").get("phoneNumber"), "%" + customerPhone + "%"));
        }

        // Filter theo số tiền tối thiểu
        if (minAmount != null) {
            spec = spec.and((root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("totalAmount"), minAmount));
        }

        // Filter theo số tiền tối đa
        if (maxAmount != null) {
            spec = spec.and((root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("totalAmount"), maxAmount));
        }

        return orderCoreService.getOrdersWithSpecification(spec, pageRequest);
    }
}
