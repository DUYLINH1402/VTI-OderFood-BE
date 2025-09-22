package com.foodorder.backend.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.foodorder.backend.order.entity.OrderStatus;
import com.foodorder.backend.util.MessageUtil;

/**
 * DTO cho WebSocket message khi có thay đổi trạng thái đơn hàng
 * Gửi real-time notification cho staff về đơn hàng mới hoặc thay đổi trạng thái
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderWebSocketMessage {

    private String messageType;
    private Long orderId;
    private String orderCode;
    private String orderStatus;
    // Trạng thái cũ (nếu là thay đổi trạng thái)
    private String previousStatus;
    private String customerName;
    private String customerPhone;
    private Double totalAmount;
    private Long timestamp;

    // Thông tin khu vực đầy đủ
    private Long wardId;
    private String wardName;
    private Long districtId;
    private String districtName;

    // Zone ID (deprecated, giữ để tương thích ngược)
    @Deprecated
    private Long zoneId;
    private String message;

    private Long customerId; // ID của user/customer
    private Long userId;     // Alias cho customerId (để linh hoạt)

    /**
     * Lấy description tiếng Việt từ OrderStatus enum
     */
    private static String getStatusDescription(String statusCode) {
        if (statusCode == null) return statusCode;

        try {
            OrderStatus status = OrderStatus.valueOf(statusCode);
            return status.getDescription();
        } catch (IllegalArgumentException e) {
            return statusCode; // Trả về code gốc nếu không tìm thấy
        }
    }

    /**
     * Tạo message cho đơn hàng mới với thông tin ward và district đầy đủ
     */
    public static OrderWebSocketMessage newOrder(Long orderId, String orderCode,
                                                String customerName, String customerPhone,
                                                Double totalAmount, Long wardId, String wardName,
                                                Long districtId, String districtName) {
        return OrderWebSocketMessage.builder()
                .messageType("NEW_ORDER")
                .orderId(orderId)
                .orderCode(orderCode)
                .orderStatus("PROCESSING")
                .customerName(customerName)
                .customerPhone(customerPhone)
                .totalAmount(totalAmount)
                .wardId(wardId)
                .wardName(wardName)
                .districtId(districtId)
                .districtName(districtName)
                .zoneId(wardId) // Để tương thích ngược
                .timestamp(System.currentTimeMillis())
                .message(MessageUtil.createStaffMessage(orderCode, "PROCESSING", null, customerName) +
                        " - Khu vực: " + (wardName != null ? wardName + ", " : "") +
                        (districtName != null ? districtName : ""))
                .build();
    }

    /**
     * Tạo message cho đơn hàng mới (method cũ để tương thích ngược)
     */
    @Deprecated
    public static OrderWebSocketMessage newOrder(Long orderId, String orderCode,
                                                String customerName, String customerPhone,
                                                Double totalAmount, Long zoneId) {
        return OrderWebSocketMessage.builder()
                .messageType("NEW_ORDER")
                .orderId(orderId)
                .orderCode(orderCode)
                .orderStatus("PROCESSING")
                .customerName(customerName)
                .customerPhone(customerPhone)
                .totalAmount(totalAmount)
                .zoneId(zoneId)
                .timestamp(System.currentTimeMillis())
                .message("Có đơn hàng mới: " + orderCode)
                .build();
    }

    /**
     * Tạo message cho thay đổi trạng thái đơn hàng với thông tin ward và district
     */
    public static OrderWebSocketMessage statusChanged(Long orderId, String orderCode,
                                                     String newStatus, String oldStatus,
                                                     String customerName, Long wardId, String wardName,
                                                     Long districtId, String districtName,
                                                     Long customerId) {
        return OrderWebSocketMessage.builder()
                .messageType("ORDER_STATUS_CHANGED")
                .orderId(orderId)
                .orderCode(orderCode)
                .orderStatus(newStatus)
                .previousStatus(oldStatus)
                .customerName(customerName)
                .wardId(wardId)
                .wardName(wardName)
                .districtId(districtId)
                .districtName(districtName)
                .customerId(customerId)
                .userId(customerId) // Alias cho customerId
                .zoneId(wardId) // Để tương thích ngược
                .timestamp(System.currentTimeMillis())
                .message(MessageUtil.createStaffMessage(orderCode, newStatus, oldStatus, customerName))
                .build();
    }

    /**
     * Tạo message cho thay đổi trạng thái đơn hàng (method cũ để tương thích ngược)
     */
    @Deprecated
    public static OrderWebSocketMessage statusChanged(Long orderId, String orderCode,
                                                     String newStatus, String oldStatus,
                                                     String customerName, Long wardId, String wardName,
                                                     Long districtId, String districtName) {
        return statusChanged(orderId, orderCode, newStatus, oldStatus, customerName,
                           wardId, wardName, districtId, districtName, null);
    }

    /**
     * Tạo message dành riêng cho khách hàng (customer notification)
     */
    public static OrderWebSocketMessage customerNotification(Long orderId, String orderCode,
                                                           String newStatus, String oldStatus,
                                                           Long customerId) {
        return OrderWebSocketMessage.builder()
                .messageType("CUSTOMER_ORDER_UPDATE")
                .orderId(orderId)
                .orderCode(orderCode)
                .orderStatus(newStatus)
                .previousStatus(oldStatus)
                .customerId(customerId)
                .userId(customerId)
                .timestamp(System.currentTimeMillis())
                .message(MessageUtil.createCustomerMessage(orderCode, newStatus, oldStatus))
                .build();
    }
}
