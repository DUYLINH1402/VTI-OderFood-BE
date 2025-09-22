package com.foodorder.backend.notifications.service;

import com.foodorder.backend.notifications.dto.NotificationCreateDTO;
import com.foodorder.backend.notifications.dto.NotificationType;
import com.foodorder.backend.notifications.entity.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Helper class để tạo thông báo cho các sự kiện khác nhau
 * Hỗ trợ tạo thông báo cho cả User và Staff
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationHelper {

    private final NotificationService notificationService;

    // ============ USER NOTIFICATIONS ============

    /**
     * Tạo thông báo khi đơn hàng được xác nhận (cho User)
     */
    public void createOrderConfirmedNotificationForUser(Long userId, Long orderId, String orderCode) {
        NotificationCreateDTO notification = NotificationCreateDTO.builder()
                .userId(userId)
                .orderId(orderId)
                .orderCode(orderCode)
                .title("Đơn hàng đã được xác nhận")
                .message(String.format("Đơn hàng %s của bạn đã được xác nhận!", orderCode))
                .type(NotificationType.ORDER_CONFIRMED.getCode())
                .recipientType(Notification.RecipientType.USER)
                .recipientId(userId)
                .build();

        notificationService.createNotification(notification);
//        log.info("Đã tạo thông báo xác nhận đơn hàng {} cho user {}", orderCode, userId);
    }

    /**
     * Tạo thông báo khi đơn hàng đang được chuẩn bị (cho User)
     */
//    public void createOrderPreparingNotificationForUser(Long userId, Long orderId, String orderCode) {
//        NotificationCreateDTO notification = NotificationCreateDTO.builder()
//                .userId(userId)
//                .orderId(orderId)
//                .orderCode(orderCode)
//                .title("Đơn hàng đang được chuẩn bị")
//                .message(String.format("Đơn hàng %s đang được nhà bếp chuẩn bị. Vui lòng chờ trong giây lát.", orderCode))
//                .type(NotificationType.ORDER_PREPARING.getCode())
//                .recipientType(Notification.RecipientType.USER)
//                .recipientId(userId)
//                .build();
//
//        notificationService.createNotification(notification);
//    }

    /**
     * Tạo thông báo khi đơn hàng đã được giao thành công (cho User)
     */
    public void createOrderDeliveredNotificationForUser(Long userId, Long orderId, String orderCode) {
        NotificationCreateDTO notification = NotificationCreateDTO.builder()
                .userId(userId)
                .orderId(orderId)
                .orderCode(orderCode)
                .title("Đơn hàng đã được giao thành công")
                .message(String.format("Đơn hàng %s đã được giao thành công!", orderCode))
                .type(NotificationType.ORDER_DELIVERED.getCode())
                .recipientType(Notification.RecipientType.USER)
                .recipientId(userId)
                .build();

        notificationService.createNotification(notification);
    }

    /**
     * Tạo thông báo thanh toán thành công (cho User)
     */
    public void createPaymentSuccessNotificationForUser(Long userId, Long orderId, String orderCode, String amount) {
        NotificationCreateDTO notification = NotificationCreateDTO.builder()
                .userId(userId)
                .orderId(orderId)
                .orderCode(orderCode)
                .title("Thanh toán thành công")
                .message(String.format("Thanh toán cho đơn hàng %s đã thành công với số tiền %s VNĐ.", orderCode, amount))
                .type(NotificationType.PAYMENT_SUCCESS.getCode())
                .recipientType(Notification.RecipientType.USER)
                .recipientId(userId)
                .build();

        notificationService.createNotification(notification);
    }

    // ============ STAFF NOTIFICATIONS ============

    /**
     * Tạo thông báo đơn hàng mới cho Staff
     * @param staffUserId ID của user có role STAFF (không phải staffId riêng biệt)
     */
    public void createNewOrderNotificationForStaff(Long staffUserId, Long orderId, String orderCode, String customerName) {
        NotificationCreateDTO notification = NotificationCreateDTO.builder()
                .userId(staffUserId) // Sử dụng userId vì trong DB user_id NOT NULL
                .orderId(orderId)
                .orderCode(orderCode)
                .title("Đơn hàng mới")
                .message(String.format("Có đơn hàng mới %s từ khách hàng %s cần được xử lý.", orderCode, customerName))
                .type("NEW_ORDER")
                .recipientType(Notification.RecipientType.STAFF)
                .recipientId(staffUserId) // Cùng ID với userId
                .build();

        notificationService.createNotification(notification);
    }

    // ============ BROADCAST NOTIFICATIONS ============

    /**
     * Tạo thông báo cho cả User và Staff về cùng một đơn hàng
     */
    public void createOrderStatusNotificationForBoth(Long userId, Long staffUserId, Long orderId, String orderCode,
                                                     String userTitle, String userMessage,
                                                     String staffTitle, String staffMessage,
                                                     String type) {
        // Thông báo cho User
        createOrderStatusNotificationForUser(userId, orderId, orderCode, userTitle, userMessage, type);

        // Thông báo cho Staff - sử dụng staffUserId thay vì staffId
        createOrderStatusNotificationForStaff(staffUserId, orderId, orderCode, staffTitle, staffMessage, type);
    }

    // ============ LEGACY METHODS (Backward Compatibility) ============

    /**
     * @deprecated Sử dụng createOrderConfirmedNotificationForUser thay thế
     */
    @Deprecated
    public void createOrderConfirmedNotification(Long userId, Long orderId, String orderCode) {
        createOrderConfirmedNotificationForUser(userId, orderId, orderCode);
    }

    /**
     * @deprecated Sử dụng createOrderPreparingNotificationForUser thay thế
     */
//    @Deprecated
//    public void createOrderPreparingNotification(Long userId, Long orderId, String orderCode) {
//        createOrderPreparingNotificationForUser(userId, orderId, orderCode);
//    }

    // ============ ORDER STATUS UPDATE NOTIFICATIONS ============

    /**
     * Tạo thông báo cập nhật trạng thái đơn hàng cho User
     */
    public void createOrderStatusNotificationForUser(Long userId, Long orderId, String orderCode,
                                                    String title, String message, String type) {
        NotificationCreateDTO notification = NotificationCreateDTO.builder()
                .userId(userId)
                .orderId(orderId)
                .orderCode(orderCode)
                .title(title)
                .message(message)
                .type(type)
                .recipientType(Notification.RecipientType.USER)
                .recipientId(userId)
                .build();

        notificationService.createNotification(notification);
    }

    /**
     * Tạo thông báo cập nhật trạng thái đơn hàng cho Staff
     * @param staffUserId ID của user có role STAFF
     */
    public void createOrderStatusNotificationForStaff(Long staffUserId, Long orderId, String orderCode,
                                                     String title, String message, String type) {
        NotificationCreateDTO notification = NotificationCreateDTO.builder()
                .userId(staffUserId) // Thêm userId để tránh lỗi user_id cannot be null
                .orderId(orderId)
                .orderCode(orderCode)
                .title(title)
                .message(message)
                .type(type)
                .recipientType(Notification.RecipientType.STAFF)
                .recipientId(staffUserId)
                .build();

        notificationService.createNotification(notification);
    }
}
