package com.foodorder.backend.order.service.impl;

import com.foodorder.backend.order.dto.request.AdminCancelOrderRequest;
import com.foodorder.backend.order.dto.request.UpdateInternalNoteRequest;
import com.foodorder.backend.order.dto.request.UpdateOrderStatusRequest;
import com.foodorder.backend.order.dto.response.AdminDashboardStatsResponse;
import com.foodorder.backend.order.dto.response.OrderResponse;
import com.foodorder.backend.order.dto.response.OrderStatisticsResponse;
import com.foodorder.backend.order.dto.response.PageResponse;
import com.foodorder.backend.order.entity.Order;
import com.foodorder.backend.order.entity.OrderStatus;
import com.foodorder.backend.order.entity.PaymentStatus;
import com.foodorder.backend.order.repository.OrderRepository;
import com.foodorder.backend.order.service.AdminOrderService;
import com.foodorder.backend.order.service.OrderCoreService;
import com.foodorder.backend.order.util.OrderMapper;
import com.foodorder.backend.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

import static com.foodorder.backend.config.CacheConfig.*;

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
    private final OrderRepository orderRepository;

    @Override
    @Cacheable(value = ADMIN_ORDERS_CACHE, key = "'orders_' + #status + '_' + #orderCode + '_' + #customerName + '_' + #startDate + '_' + #endDate + '_' + #staffId + '_' + #pageRequest.pageNumber + '_' + #pageRequest.pageSize")
    public PageResponse<OrderResponse> getAllOrdersWithFilters(
            String status, String orderCode, String customerName,
            String startDate, String endDate, Long staffId, PageRequest pageRequest) {

        Specification<Order> spec = orderCoreService.createOrderSpecification(
            status, orderCode, customerName, startDate, endDate, staffId);

        return orderCoreService.getOrdersWithSpecification(spec, pageRequest);
    }

    @Override
    @Cacheable(value = ORDER_STATISTICS_CACHE, key = "'stats_' + #startDate + '_' + #endDate + '_' + #period")
    public OrderStatisticsResponse getOrderStatistics(String startDate, String endDate, String period) {
        return orderCoreService.calculateOrderStatistics(startDate, endDate, period);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = ADMIN_ORDERS_CACHE, allEntries = true),
            @CacheEvict(value = ORDER_STATISTICS_CACHE, allEntries = true),
            @CacheEvict(value = DASHBOARD_STATISTICS_CACHE, allEntries = true),
            @CacheEvict(value = DASHBOARD_REVENUE_CACHE, allEntries = true)
    })
    public OrderResponse updateOrderStatusWithFullAccess(Long orderId, UpdateOrderStatusRequest request) {
        // Admin có quyền cập nhật tất cả trạng thái
        Set<OrderStatus> allowedStatuses = orderCoreService.getAllowedStatusesForRole("ROLE_ROLE_ADMIN");

        Order updatedOrder = orderCoreService.updateOrderStatusWithValidation(
            orderId, request, allowedStatuses);

        return orderMapper.toOrderResponse(updatedOrder);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = ADMIN_ORDERS_CACHE, allEntries = true),
            @CacheEvict(value = ORDER_STATISTICS_CACHE, allEntries = true),
            @CacheEvict(value = DASHBOARD_STATISTICS_CACHE, allEntries = true),
            @CacheEvict(value = DASHBOARD_REVENUE_CACHE, allEntries = true)
    })
    public void deleteOrder(Long orderId) {
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
    @Caching(evict = {
            @CacheEvict(value = ADMIN_ORDERS_CACHE, allEntries = true),
            @CacheEvict(value = ORDER_STATISTICS_CACHE, allEntries = true),
            @CacheEvict(value = DASHBOARD_STATISTICS_CACHE, allEntries = true),
            @CacheEvict(value = DASHBOARD_REVENUE_CACHE, allEntries = true)
    })
    public OrderResponse restoreOrder(Long orderId) {
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
        Order order = orderCoreService.findOrderByIdWithValidation(orderId);
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Cacheable(value = ADMIN_ORDERS_CACHE, key = "'search_' + #keyword + '_' + #status + '_' + #customerEmail + '_' + #customerPhone + '_' + #minAmount + '_' + #maxAmount + '_' + #pageRequest.pageNumber + '_' + #pageRequest.pageSize")
    public PageResponse<OrderResponse> advancedSearch(
            String keyword, String status, String customerEmail, String customerPhone,
            Double minAmount, Double maxAmount, PageRequest pageRequest) {

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

    // ============ CÁC API MỚI CHO ADMIN ============

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = ADMIN_ORDERS_CACHE, allEntries = true),
            @CacheEvict(value = ORDER_STATISTICS_CACHE, allEntries = true),
            @CacheEvict(value = DASHBOARD_STATISTICS_CACHE, allEntries = true)
    })
    public OrderResponse updateInternalNote(Long orderId, UpdateInternalNoteRequest request) {
        log.info("Admin cập nhật ghi chú nội bộ cho đơn hàng ID: {}", orderId);

        Order order = orderCoreService.findOrderByIdWithValidation(orderId);

        // Cập nhật ghi chú nội bộ
        order.setInternalNote(request.getInternalNote());

        Order savedOrder = orderRepository.save(order);
        log.info("Đã cập nhật ghi chú nội bộ cho đơn hàng ID: {}", orderId);

        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = ADMIN_ORDERS_CACHE, allEntries = true),
            @CacheEvict(value = ORDER_STATISTICS_CACHE, allEntries = true),
            @CacheEvict(value = DASHBOARD_STATISTICS_CACHE, allEntries = true),
            @CacheEvict(value = DASHBOARD_REVENUE_CACHE, allEntries = true)
    })
    public OrderResponse cancelOrderWithReason(Long orderId, AdminCancelOrderRequest request) {
        log.info("Admin hủy đơn hàng ID: {} với lý do: {}", orderId, request.getCancelReason());

        Order order = orderCoreService.findOrderByIdWithValidation(orderId);

        // Kiểm tra đơn hàng đã bị hủy chưa
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new ResourceNotFoundException("ORDER_ALREADY_CANCELLED", "Đơn hàng đã bị hủy trước đó");
        }

        // Kiểm tra đơn hàng đã hoàn thành chưa
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new ResourceNotFoundException("ORDER_ALREADY_COMPLETED", "Không thể hủy đơn hàng đã hoàn thành");
        }

        // Cập nhật trạng thái hủy
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(request.getCancelReason());
        order.setCancelledAt(LocalDateTime.now());

        // Cập nhật ghi chú nội bộ nếu có
        if (request.getInternalNote() != null && !request.getInternalNote().isEmpty()) {
            String existingNote = order.getInternalNote();
            String newNote = request.getInternalNote();
            if (existingNote != null && !existingNote.isEmpty()) {
                order.setInternalNote(existingNote + "\n---\n[Hủy đơn] " + newNote);
            } else {
                order.setInternalNote("[Hủy đơn] " + newNote);
            }
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Đã hủy đơn hàng ID: {} với lý do: {}", orderId, request.getCancelReason());

        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    @Cacheable(value = DASHBOARD_STATISTICS_CACHE, key = "'admin_dashboard_stats'")
    public AdminDashboardStatsResponse getDashboardStats() {
        log.info("Lấy thống kê dashboard cho Admin");

        // Tính các mốc thời gian
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = LocalDate.now().atTime(LocalTime.MAX);

        LocalDateTime startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime endOfWeek = LocalDate.now().with(DayOfWeek.SUNDAY).atTime(LocalTime.MAX);

        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()).atTime(LocalTime.MAX);

        // Kỳ trước để tính % tăng trưởng (tháng trước)
        LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDateTime endOfLastMonth = startOfMonth.minusSeconds(1);

        // === THỐNG KÊ DOANH THU ===
        BigDecimal actualRevenue = orderRepository.getActualRevenue();
        BigDecimal totalRevenue = orderRepository.getTotalRevenueAll();
        BigDecimal revenueToday = orderRepository.getActualRevenueByDateRange(startOfToday, endOfToday);
        BigDecimal revenueThisWeek = orderRepository.getActualRevenueByDateRange(startOfWeek, endOfWeek);
        BigDecimal revenueThisMonth = orderRepository.getActualRevenueByDateRange(startOfMonth, endOfMonth);
        BigDecimal revenueLastMonth = orderRepository.getActualRevenueByDateRange(startOfLastMonth, endOfLastMonth);

        // Tính % tăng trưởng so với tháng trước
        BigDecimal revenueGrowthPercent = BigDecimal.ZERO;
        if (revenueLastMonth != null && revenueLastMonth.compareTo(BigDecimal.ZERO) > 0) {
            revenueGrowthPercent = revenueThisMonth.subtract(revenueLastMonth)
                    .divide(revenueLastMonth, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // === THỐNG KÊ ĐƠN HÀNG ===
        Long totalOrders = orderRepository.count();
        Long ordersToday = orderRepository.countOrdersInDateRange(startOfToday, endOfToday);
        Long ordersThisWeek = orderRepository.countOrdersInDateRange(startOfWeek, endOfWeek);
        Long ordersThisMonth = orderRepository.countOrdersInDateRange(startOfMonth, endOfMonth);

        // === THỐNG KÊ ĐƠN THEO TRẠNG THÁI ===
        Long pendingOrders = orderRepository.countByStatus(OrderStatus.PENDING);
        Long processingOrders = orderRepository.countByStatus(OrderStatus.PROCESSING);
        Long confirmedOrders = orderRepository.countByStatus(OrderStatus.CONFIRMED);
        Long deliveringOrders = orderRepository.countByStatus(OrderStatus.DELIVERING);
        Long completedOrders = orderRepository.countByStatus(OrderStatus.COMPLETED);
        Long cancelledOrders = orderRepository.countByStatus(OrderStatus.CANCELLED);
        Long cancelledOrdersToday = orderRepository.countCancelledOrdersInDateRange(startOfToday, endOfToday);
        Long cancelledOrdersThisWeek = orderRepository.countCancelledOrdersInDateRange(startOfWeek, endOfWeek);

        // Tỷ lệ hủy đơn
        Double cancellationRate = 0.0;
        if (totalOrders > 0) {
            cancellationRate = (cancelledOrders.doubleValue() / totalOrders.doubleValue()) * 100;
        }

        // === THỐNG KÊ GHI CHÚ NỘI BỘ ===
        Long ordersWithInternalNotes = orderRepository.countOrdersWithInternalNotes();
        Long newInternalNotesToday = orderRepository.countOrdersWithInternalNotesInDateRange(startOfToday, endOfToday);
        Long newInternalNotesThisWeek = orderRepository.countOrdersWithInternalNotesInDateRange(startOfWeek, endOfWeek);

        // === THỐNG KÊ ĐIỂM THƯỞNG ===
        Long totalPointsUsed = orderRepository.getTotalPointsUsed();
        BigDecimal totalPointsDiscount = orderRepository.getTotalPointsDiscount();

        // === THỐNG KÊ COUPON ===
        Long ordersWithCoupon = orderRepository.countOrdersWithCoupon();
        BigDecimal totalCouponDiscount = orderRepository.getTotalCouponDiscount();

        // === THỐNG KÊ THANH TOÁN ===
        Long paidOrders = orderRepository.countByPaymentStatus(PaymentStatus.PAID);
        Long unpaidOrders = orderRepository.countByPaymentStatus(PaymentStatus.PENDING);
        Long refundedOrders = orderRepository.countByPaymentStatus(PaymentStatus.REFUNDED);

        // === GIÁ TRỊ TRUNG BÌNH ===
        BigDecimal averageOrderValue = orderRepository.getAverageOrderValue();

        return AdminDashboardStatsResponse.builder()
                // Doanh thu
                .actualRevenue(actualRevenue)
                .totalRevenue(totalRevenue)
                .revenueToday(revenueToday)
                .revenueThisWeek(revenueThisWeek)
                .revenueThisMonth(revenueThisMonth)
                .revenueGrowthPercent(revenueGrowthPercent)
                // Đơn hàng
                .totalOrders(totalOrders)
                .ordersToday(ordersToday)
                .ordersThisWeek(ordersThisWeek)
                .ordersThisMonth(ordersThisMonth)
                // Trạng thái đơn
                .pendingOrders(pendingOrders)
                .processingOrders(processingOrders)
                .confirmedOrders(confirmedOrders)
                .deliveringOrders(deliveringOrders)
                .completedOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .cancelledOrdersToday(cancelledOrdersToday)
                .cancelledOrdersThisWeek(cancelledOrdersThisWeek)
                .cancellationRate(cancellationRate)
                // Ghi chú nội bộ
                .ordersWithInternalNotes(ordersWithInternalNotes)
                .newInternalNotesToday(newInternalNotesToday)
                .newInternalNotesThisWeek(newInternalNotesThisWeek)
                // Điểm thưởng
                .totalPointsUsed(totalPointsUsed)
                .totalPointsDiscount(totalPointsDiscount)
                // Coupon
                .ordersWithCoupon(ordersWithCoupon)
                .totalCouponDiscount(totalCouponDiscount)
                // Thanh toán
                .paidOrders(paidOrders)
                .unpaidOrders(unpaidOrders)
                .refundedOrders(refundedOrders)
                // Giá trị trung bình
                .averageOrderValue(averageOrderValue)
                .build();
    }
}
