package com.foodorder.backend.order.service.impl;

import com.foodorder.backend.order.dto.request.UpdateOrderStatusRequest;
import com.foodorder.backend.order.dto.response.OrderResponse;
import com.foodorder.backend.order.dto.response.OrderStatisticsResponse;
import com.foodorder.backend.order.dto.response.PageResponse;
import com.foodorder.backend.order.entity.Order;
import com.foodorder.backend.order.entity.OrderItem;
import com.foodorder.backend.order.entity.OrderStatus;
import com.foodorder.backend.order.repository.OrderRepository;
import com.foodorder.backend.order.repository.OrderItemRepository;
import com.foodorder.backend.order.service.OrderCoreService;
import com.foodorder.backend.order.util.OrderMapper;
import com.foodorder.backend.food.repository.FoodRepository;
import com.foodorder.backend.exception.ResourceNotFoundException;
import com.foodorder.backend.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Core Service Implementation chứa logic nghiệp vụ chung
 * Được sử dụng bởi StaffOrderService và AdminOrderService
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderCoreServiceImpl implements OrderCoreService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final OrderItemRepository orderItemRepository;
    private final FoodRepository foodRepository;

    @Override
    public Order findOrderByIdWithValidation(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND",
                    "Không tìm thấy đơn hàng với ID: " + orderId));
    }

    @Override
    public Order findOrderByCode(String orderCode) {
        return orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("ORDER_NOT_FOUND",
                    "Không tìm thấy đơn hàng với mã: " + orderCode));
    }

    @Override
    public PageResponse<OrderResponse> getOrdersWithSpecification(
            Specification<Order> spec, PageRequest pageRequest) {

        Page<Order> orderPage = orderRepository.findAll(spec, pageRequest);

        // Sử dụng OrderMapper để chuyển đổi Page<Order> sang PageResponse<OrderResponse> lấy được đầy đủ dữ liệu hơn
        List<OrderResponse> orderResponses = orderMapper.toOrderResponseList(orderPage.getContent());
        return PageResponse.<OrderResponse>builder()
                .data(orderResponses)
                .page(orderPage.getNumber())
                .size(orderPage.getSize())
                .total(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .hasNext(!orderPage.isLast())
                .hasPrevious(!orderPage.isFirst())
                .build();
    }

    @Override
    public Specification<Order> createOrderSpecification(
            String status, String orderCode, String customerName,
            String startDate, String endDate, Long staffId) {

        Specification<Order> spec = Specification.where(null);

        // Filter theo status
        if (!"all".equals(status)) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), orderStatus));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid order status: {}", status);
            }
        }

        // Filter theo order code
        if (orderCode != null && !orderCode.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                cb.like(cb.lower(root.get("orderCode")), "%" + orderCode.toLowerCase() + "%"));
        }

        // Filter theo tên khách hàng
        if (customerName != null && !customerName.isEmpty()) {
            spec = spec.and((root, query, cb) ->
                cb.like(cb.lower(root.get("user").get("fullName")), "%" + customerName.toLowerCase() + "%"));
        }

        // Filter theo ngày tạo
        if (startDate != null && !startDate.isEmpty()) {
            LocalDateTime startDateTime = LocalDate.parse(startDate).atStartOfDay();
            spec = spec.and((root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("createdAt"), startDateTime));
        }

        if (endDate != null && !endDate.isEmpty()) {
            LocalDateTime endDateTime = LocalDate.parse(endDate).atTime(23, 59, 59);
            spec = spec.and((root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("createdAt"), endDateTime));
        }

        return spec;
    }

    @Override
    @Transactional
    public Order updateOrderStatusWithValidation(
            Long orderId, UpdateOrderStatusRequest request,
            Set<OrderStatus> allowedStatuses) {

        Order order = findOrderByIdWithValidation(orderId);
        OrderStatus newStatus;

        try {
            newStatus = OrderStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("INVALID_STATUS",
                "Trạng thái không hợp lệ: " + request.getStatus());
        }

        // Kiểm tra quyền cập nhật trạng thái
        if (!allowedStatuses.contains(newStatus)) {
            throw new BadRequestException("STATUS_NOT_ALLOWED",
                "Bạn không có quyền cập nhật trạng thái này: " + newStatus);
        }

        // Validate transition cho Staff (Admin có thể bypass)
        if (!isValidStatusTransition(order.getStatus(), newStatus, "ROLE_STAFF")) {
            throw new BadRequestException("INVALID_TRANSITION",
                "Không thể chuyển từ trạng thái " + order.getStatus() + " sang " + newStatus);
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        // Cập nhật ghi chú nếu có
        if (request.getNote() != null && !request.getNote().isEmpty()) {
            order.setStaffNote(request.getNote()); // Sử dụng staffNote thay vì note
        }

        Order savedOrder = orderRepository.save(order);

        // Cập nhật totalSold cho các món ăn khi đơn hàng hoàn thành
        if (newStatus == OrderStatus.COMPLETED && oldStatus != OrderStatus.COMPLETED) {
            updateFoodTotalSold(savedOrder.getId());
        }

        // Gửi notification
        sendOrderStatusNotification(savedOrder, oldStatus);

        return savedOrder;
    }

    /**
     * Cập nhật totalSold cho các món ăn trong đơn hàng đã hoàn thành
     * Được gọi khi đơn hàng chuyển sang trạng thái COMPLETED
     * @param orderId ID của đơn hàng
     */
    private void updateFoodTotalSold(Long orderId) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);

        for (OrderItem item : orderItems) {
            if (item.getFoodId() != null && item.getQuantity() != null && item.getQuantity() > 0) {
                foodRepository.incrementTotalSold(item.getFoodId(), item.getQuantity());
                log.info("Đã cập nhật totalSold cho món ăn ID: {} với số lượng: {}",
                        item.getFoodId(), item.getQuantity());
            }
        }

        log.info("Đã cập nhật totalSold cho {} món ăn trong đơn hàng ID: {}",
                orderItems.size(), orderId);
    }

    @Override
    public OrderStatisticsResponse calculateOrderStatistics(
            String startDate, String endDate, String period) {

        LocalDateTime startDateTime = startDate != null ?
            LocalDate.parse(startDate).atStartOfDay() :
            LocalDateTime.now().minusDays(30);

        LocalDateTime endDateTime = endDate != null ?
            LocalDate.parse(endDate).atTime(23, 59, 59) :
            LocalDateTime.now();

        // Sử dụng query đơn giản thay vì các method chưa tồn tại
        List<Order> allOrders = orderRepository.findByCreatedAtBetween(startDateTime, endDateTime);

        long totalOrders = allOrders.size();
        long pendingOrders = allOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.PENDING)
            .count();
        long completedOrders = allOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
            .count();
        long cancelledOrders = allOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.CANCELLED)
            .count();

        BigDecimal totalRevenue = allOrders.stream()
            .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
            .map(Order::getTotalPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return OrderStatisticsResponse.builder()
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .totalRevenue(totalRevenue)
                .build();
    }

    @Override
    public boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus, String role) {
        // Admin có thể chuyển đổi mọi trạng thái
        if ("ROLE_ADMIN".equals(role)) {
            return true;
        }

        // Workflow đã cập nhật: PENDING -> PROCESSING -> CONFIRMED -> DELIVERING -> COMPLETED
        if ("ROLE_STAFF".equals(role)) {
            return switch (currentStatus) {
                case PENDING -> Set.of(OrderStatus.CANCELLED).contains(newStatus); // PENDING chỉ có thể hủy, không thể xác nhận trực tiếp
                case PROCESSING -> Set.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED).contains(newStatus); // Từ PROCESSING có thể xác nhận hoặc hủy
                case CONFIRMED -> Set.of(OrderStatus.DELIVERING, OrderStatus.CANCELLED).contains(newStatus);
                case DELIVERING -> Set.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED).contains(newStatus);
                case COMPLETED, CANCELLED -> false; // Không thể thay đổi từ trạng thái cuối
                default -> false;
            };
        }

        return false;
    }

    @Override
    public Set<OrderStatus> getAllowedStatusesForRole(String role) {
        if ("ROLE_ADMIN".equals(role)) {
            // Admin có thể cập nhật tất cả trạng thái
            return Set.of(OrderStatus.values());
        }

        if ("ROLE_STAFF".equals(role)) {
            // Staff chỉ có thể cập nhật các trạng thái trong workflow
            return Set.of(
                OrderStatus.CONFIRMED,
                OrderStatus.DELIVERING,
                OrderStatus.COMPLETED,
                OrderStatus.CANCELLED
            );
        }

        return Set.of();
    }

    @Override
    public void sendOrderStatusNotification(Order order, OrderStatus oldStatus) {
        // Đảm bảo order có orderCode, nếu không thì tạo mới
        if (order.getOrderCode() == null || order.getOrderCode().isEmpty()) {
            order.setOrderCode(generateOrderCodeForExistingOrder(order.getId()));
            orderRepository.save(order); // Lưu lại orderCode vừa tạo
        }

        // Gửi email/SMS notification cho customer
        // Gửi real-time notification cho staff/admin
    }

    // Helper method để tạo orderCode cho đơn hàng đã tồn tại
    private String generateOrderCodeForExistingOrder(Long orderId) {
        return String.format("ORD%08d", orderId);
    }
}
