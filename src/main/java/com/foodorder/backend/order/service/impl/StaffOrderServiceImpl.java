package com.foodorder.backend.order.service.impl;

import com.foodorder.backend.order.dto.request.UpdateOrderStatusRequest;
import com.foodorder.backend.order.dto.response.OrderResponse;
import com.foodorder.backend.order.dto.response.PageResponse;
import com.foodorder.backend.order.dto.OrderWebSocketMessage;
import com.foodorder.backend.order.entity.Order;
import com.foodorder.backend.order.entity.OrderStatus;
import com.foodorder.backend.order.service.OrderCoreService;
import com.foodorder.backend.order.service.StaffOrderService;
import com.foodorder.backend.order.util.OrderMapper;
import com.foodorder.backend.service.WebSocketService;
import com.foodorder.backend.notifications.service.NotificationHelper;
import com.foodorder.backend.user.repository.UserRepository;
import com.foodorder.backend.user.entity.User;
import com.foodorder.backend.zone.repository.WardRepository;
import com.foodorder.backend.zone.repository.DistrictRepository;
import com.foodorder.backend.zone.entity.Ward;
import com.foodorder.backend.zone.entity.District;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Staff Order Service Implementation
 * Sử dụng OrderCoreService cho logic chung và thêm logic riêng cho Staff
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StaffOrderServiceImpl implements StaffOrderService {

    private final OrderCoreService orderCoreService;
    private final OrderMapper orderMapper;
    private final WebSocketService webSocketService;
    private final WardRepository wardRepository;
    private final DistrictRepository districtRepository;
    private final NotificationHelper notificationHelper;
    private final UserRepository userRepository;

    @Override
    public PageResponse<OrderResponse> getOrdersNeedConfirmation(PageRequest pageRequest) {

        // Tạo spec cho đơn hàng cần xác nhận (PROCESSING - đã thanh toán, chờ xác nhận)
        Specification<Order> spec = Specification.where(
            (root, query, cb) -> cb.equal(root.get("status"), OrderStatus.PROCESSING)
        );

        return orderCoreService.getOrdersWithSpecification(spec, pageRequest);
    }

    @Override
    public PageResponse<OrderResponse> getProcessingOrders(PageRequest pageRequest) {
        // Tạo spec cho đơn hàng đang xử lý (CONFIRMED và DELIVERING)
        Specification<Order> spec = Specification.where(
            (root, query, cb) -> cb.or(
                cb.equal(root.get("status"), OrderStatus.CONFIRMED),   // Đang chế biến
                cb.equal(root.get("status"), OrderStatus.DELIVERING)   // Đang giao
            )
        );

        return orderCoreService.getOrdersWithSpecification(spec, pageRequest);
    }


    @Override
    @Transactional
    public OrderResponse updateOrderStatusByCode(String orderCode, UpdateOrderStatusRequest request) {
        // Tìm đơn hàng theo orderCode
        Order order = orderCoreService.findOrderByCode(orderCode);
        String oldStatus = order.getStatus().toString();

        // Lấy trạng thái được phép cho Staff
        Set<OrderStatus> allowedStatuses = orderCoreService.getAllowedStatusesForRole("ROLE_STAFF");

        Order updatedOrder = orderCoreService.updateOrderStatusWithValidation(
            order.getId(), request, allowedStatuses);

        // **GỬI THÔNG BÁO WEBSOCKET VÀ LUU VÀO DATABASE CHO USER**
        try {
            // Chỉ gửi thông báo cho customer, không gửi cho staff để đơn giản hóa
            if (updatedOrder.getUserId() != null) {
                try {
                    // Gửi WebSocket notification cho customer
                    OrderWebSocketMessage customerMessage = OrderWebSocketMessage.customerNotification(
                        updatedOrder.getId(),
                        updatedOrder.getOrderCode(),
                        updatedOrder.getStatus().toString(),
                        oldStatus,
                        updatedOrder.getUserId()
                    );
                    webSocketService.sendNotificationToUser(updatedOrder.getUserId(), customerMessage);

                    // Lưu thông báo vào database cho customer
                    createOrderStatusNotificationForUser(updatedOrder, oldStatus);

                } catch (Exception userNotificationEx) {
                    log.error("Lỗi khi gửi thông báo cho user {} về đơn hàng {}: {}",
                            updatedOrder.getUserId(), updatedOrder.getOrderCode(), userNotificationEx.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Lỗi khi gửi thông báo cho đơn hàng {}: {}",
                    updatedOrder.getOrderCode(), e.getMessage());
        }

        return orderMapper.toOrderResponse(updatedOrder);
    }

    @Override
    public OrderResponse getOrderDetails(String orderIdOrCode) {
        // Tự động phát hiện và xử lý theo ID hoặc mã đơn hàng
        Order order;
        try {
            // Thử parse thành Long trước (nếu là ID)
            Long orderId = Long.parseLong(orderIdOrCode);
            order = orderCoreService.findOrderByIdWithValidation(orderId);
        } catch (NumberFormatException e) {
            // Nếu không parse được thành Long, coi như là orderCode
            order = orderCoreService.findOrderByCode(orderIdOrCode);
        }
        return orderMapper.toOrderResponse(order);
    }

    @Override
    public PageResponse<OrderResponse> getRecentOrders(PageRequest pageRequest) {
        // Lấy đơn hàng trong 7 ngày gần đây (loại trừ PENDING - chỉ Admin mới thấy)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        Specification<Order> spec = Specification.where(
            (root, query, cb) -> cb.and(
                cb.greaterThanOrEqualTo(root.get("createdAt"), sevenDaysAgo),
                cb.notEqual(root.get("status"), OrderStatus.PENDING)  // Loại trừ PENDING
            )
        );

        return orderCoreService.getOrdersWithSpecification(spec, pageRequest);
    }

    @Override
    public PageResponse<OrderResponse> getRecentOrdersWithFilter(PageRequest pageRequest, int days, String status, String search) {

        // Tính ngày bắt đầu dựa trên số ngày được chỉ định
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);

        // Tạo Specification cơ bản - lấy đơn hàng trong khoảng thời gian và loại trừ PENDING
        Specification<Order> spec = Specification.where(
            (root, query, cb) -> cb.and(
                cb.greaterThanOrEqualTo(root.get("createdAt"), startDate),
                cb.notEqual(root.get("status"), OrderStatus.PENDING)  // Staff không thấy đơn PENDING
            )
        );

        // Thêm bộ lọc theo trạng thái nếu có
        if (status != null && !status.trim().isEmpty()) {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), orderStatus)
                );
            } catch (IllegalArgumentException e) {
                log.warn("Invalid status filter: {}", status);
            }
        }

        // Thêm bộ lọc tìm kiếm nếu có
        if (search != null && !search.trim().isEmpty()) {
            String searchTerm = "%" + search.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("orderCode")), searchTerm),
                cb.like(cb.lower(root.get("receiverName")), searchTerm),
                cb.like(cb.lower(root.get("receiverPhone")), searchTerm),
                cb.like(cb.lower(root.get("receiverEmail")), searchTerm)
            ));
        }

        return orderCoreService.getOrdersWithSpecification(spec, pageRequest);
    }

    // ============ PRIVATE HELPER METHODS FOR NOTIFICATIONS ============

    /**
     * Tạo thông báo cập nhật trạng thái đơn hàng cho User
     */
    private void createOrderStatusNotificationForUser(Order updatedOrder, String oldStatus) {
        try {
            String newStatus = updatedOrder.getStatus().toString();
            String title = getOrderStatusTitleForUser(newStatus);
            String message = getOrderStatusMessageForUser(updatedOrder.getOrderCode(), newStatus, oldStatus);
            String notificationType = getNotificationTypeForStatus(newStatus);

            // Sử dụng NotificationHelper để tạo thông báo cho user
            notificationHelper.createOrderStatusNotificationForUser(
                updatedOrder.getUserId(),
                updatedOrder.getId(),
                updatedOrder.getOrderCode(),
                title,
                message,
                notificationType
            );

            log.info("Đã tạo thông báo cập nhật trạng thái cho user {} về đơn hàng {}: {} -> {}",
                    updatedOrder.getUserId(), updatedOrder.getOrderCode(), oldStatus, newStatus);
        } catch (Exception e) {
            log.error("Lỗi khi tạo thông báo cho user {} về đơn hàng {}: {}",
                    updatedOrder.getUserId(), updatedOrder.getOrderCode(), e.getMessage());
        }
    }

    /**
     * Lấy tiêu đề thông báo cho User dựa trên trạng thái đơn hàng
     */
    private String getOrderStatusTitleForUser(String status) {
        switch (status) {
            case "CONFIRMED":
                return "Đơn hàng đã được xác nhận";
            case "DELIVERING":
                return "Đơn hàng đang được giao";
            case "COMPLETED":
                return "Đơn hàng đã được giao thành công";
            case "CANCELLED":
                return "Đơn hàng đã bị hủy";
            default:
                return "Cập nhật trạng thái đơn hàng";
        }
    }

    /**
     * Lấy nội dung thông báo cho User dựa trên trạng thái đơn hàng
     */
    private String getOrderStatusMessageForUser(String orderCode, String newStatus, String oldStatus) {
        switch (newStatus) {
            case "CONFIRMED":
                return String.format("Đơn hàng %s của bạn đã được xác nhận và đang được chuẩn bị.", orderCode);
            case "DELIVERING":
                return String.format("Đơn hàng %s đã chuẩn bị xong. Vui lòng chuẩn bị nhận hàng.", orderCode);
            case "COMPLETED":
                return String.format("Đơn hàng %s đã được giao thành công. Cảm ơn bạn đã sử dụng dịch vụ!", orderCode);
            case "CANCELLED":
                return String.format("Đơn hàng %s đã bị hủy. Xin lỗi vì sự bất tiện này.", orderCode);
            default:
                return String.format("Trạng thái đơn hàng %s đã được cập nhật từ %s sang %s.", orderCode, oldStatus, newStatus);
        }
    }

    /**
     * Lấy loại thông báo dựa trên trạng thái đơn hàng
     */
    private String getNotificationTypeForStatus(String status) {
        switch (status) {
            case "CONFIRMED":
                return "ORDER_CONFIRMED";
            case "DELIVERING":
                return "ORDER_DELIVERING";
            case "COMPLETED":
                return "ORDER_COMPLETED";
            case "CANCELLED":
                return "ORDER_CANCELLED";
            default:
                return "ORDER_STATUS_UPDATE";
        }
    }
}
