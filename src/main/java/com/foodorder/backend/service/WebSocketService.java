package com.foodorder.backend.service;

import com.foodorder.backend.order.dto.OrderWebSocketMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service xử lý WebSocket messaging cho hệ thống đặt món
 * Chuyên xử lý việc gửi real-time notifications cho staff về đơn hàng
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Gửi thông báo đơn hàng mới cho tất cả staff online
     * @param message Thông tin đơn hàng mới
     */
    public void sendNewOrderNotification(OrderWebSocketMessage message) {
        try {

            // Gửi tới tất cả staff đang lắng nghe channel /topic/new-orders
            messagingTemplate.convertAndSend("/topic/new-orders", message);

        } catch (Exception e) {
            log.error(" LỖI khi gửi thông báo đơn hàng mới: {}", e.getMessage(), e);
            log.error(" Order Code: {}, Order ID: {}", message.getOrderCode(), message.getOrderId());
        }
    }

    /**
     * Gửi thông báo thay đổi trạng thái đơn hàng cho tất cả staff
     * @param message Thông tin thay đổi trạng thái
     */
    public void sendOrderStatusUpdate(OrderWebSocketMessage message) {
        try {

            // Gửi tới channel chung cho staff
            messagingTemplate.convertAndSend("/topic/order-updates", message);

            // Gửi riêng cho đơn hàng cụ thể (nếu có staff đang theo dõi đơn này)
            messagingTemplate.convertAndSend("/topic/order/" + message.getOrderId(), message);

        } catch (Exception e) {
            log.error(" LỖI khi gửi thông báo cập nhật trạng thái: {}", e.getMessage(), e);
        }
    }

    // =================== USER NOTIFICATIONS ===================

    /**
     * Gửi thông báo cập nhật trạng thái đơn hàng cho user (khách hàng)
     * @param userId ID của user nhận thông báo
     * @param message Thông tin cập nhật trạng thái đơn hàng
     */
    public void sendNotificationToUser(Long userId, OrderWebSocketMessage message) {
        try {

            // QUAN TRỌNG: Đảm bảo channel path đồng nhất với UserWebSocketController
            // Sử dụng convertAndSendToUser để Spring tự động thêm prefix /user/
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    "/queue/order-updates", // Spring sẽ tự động tạo thành /user/{userId}/queue/order-updates
                    message
            );

        } catch (Exception e) {
            log.error("❌ Lỗi khi gửi thông báo cho user {}: {}", userId, e.getMessage(), e);
        }
    }

}
