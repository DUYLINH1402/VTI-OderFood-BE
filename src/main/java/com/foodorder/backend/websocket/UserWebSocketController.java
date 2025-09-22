package com.foodorder.backend.websocket;

import com.foodorder.backend.order.dto.OrderWebSocketMessage;
import com.foodorder.backend.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket Controller xử lý các message real-time cho User
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class UserWebSocketController {

    private final WebSocketService webSocketService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Xử lý khi user đăng ký kênh order-updates
     * FE sẽ subscribe tới: /user/{userId}/queue/order-updates
     * Và gửi message tới: /app/user/{userId}/register-order-updates
     */
    @MessageMapping("/user/{userId}/register-order-updates")
    public void registerOrderUpdates(@DestinationVariable String userId,
                                   @Payload(required = false) String message,
                                   SimpMessageHeaderAccessor headerAccessor) {
        try {
            // Kiểm tra userId hợp lệ
            if (userId == null || userId.trim().isEmpty()) {
                log.error(" UserId không hợp lệ: {}", userId);
                return;
            }

            // Lưu thông tin user vào session
            headerAccessor.getSessionAttributes().put("userId", userId);
            String normalizedUserId = userId.trim();

            // Gửi welcome message tới user
            Map<String, Object> welcomeMessage = new HashMap<>();
            welcomeMessage.put("type", "WELCOME");
            welcomeMessage.put("message", " Bạn đã kết nối thành công! Sẵn sàng nhận thông báo đơn hàng.");
            welcomeMessage.put("userId", normalizedUserId);
            welcomeMessage.put("timestamp", LocalDateTime.now().toString());
            welcomeMessage.put("sessionId", headerAccessor.getSessionId());

            // Gửi welcome message
            messagingTemplate.convertAndSendToUser(
                normalizedUserId,
                "/queue/order-updates",
                welcomeMessage
            );

        } catch (Exception e) {
            log.error(" Lỗi khi đăng ký order-updates cho user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Xử lý khi user đăng ký WebSocket tổng quát (để tương thích với code cũ)
     */
    @MessageMapping("/user/register")
    public void registerUser(@Payload String userId, SimpMessageHeaderAccessor headerAccessor) {
        try {

            // Kiểm tra userId hợp lệ
            if (userId == null || userId.trim().isEmpty()) {
                log.error(" UserId không hợp lệ: {}", userId);
                return;
            }

            // Lưu thông tin user vào session
            headerAccessor.getSessionAttributes().put("userId", userId);
            String normalizedUserId = userId.trim();


            // Gửi thông báo welcome tới user
            Map<String, Object> welcomeMessage = new HashMap<>();
            welcomeMessage.put("type", "WELCOME");
            welcomeMessage.put("message", " Bạn đã kết nối thành công! Sẵn sàng nhận thông báo đơn hàng.");
            welcomeMessage.put("userId", normalizedUserId);
            welcomeMessage.put("timestamp", LocalDateTime.now().toString());
            welcomeMessage.put("channel", "/user/" + normalizedUserId + "/queue/order-updates");
            welcomeMessage.put("sessionId", headerAccessor.getSessionId());

            // Gửi welcome message
            messagingTemplate.convertAndSendToUser(
                normalizedUserId,
                "/queue/order-updates",
                welcomeMessage
            );


        } catch (Exception e) {
            log.error(" Lỗi khi đăng ký user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Gửi order update tới user cụ thể
     */
    public void sendOrderUpdateToUser(String userId, String orderId, String status, String message) {
        try {

            // Tạo OrderWebSocketMessage
            OrderWebSocketMessage updateMessage = OrderWebSocketMessage.builder()
                    .orderId(Long.parseLong(orderId))
                    .orderCode(orderId)
                    .orderStatus(status)
                    .message(message)
                    .customerId(Long.parseLong(userId))
                    .timestamp(System.currentTimeMillis())
                    .build();

            // Gửi qua WebSocketService
            webSocketService.sendNotificationToUser(Long.parseLong(userId), updateMessage);

        } catch (Exception e) {
            log.error("Lỗi khi gửi order update tới user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Xử lý chat từ user tới staff
     */
    @MessageMapping("/user/chat-to-staff")
    @SendTo("/topic/staff-chat")
    public Map<String, Object> chatToStaff(@Payload String chatMessage, SimpMessageHeaderAccessor headerAccessor) {
        try {
            String userId = (String) headerAccessor.getSessionAttributes().get("userId");
            log.info(" User {} gửi tin nhắn tới staff: {}", userId, chatMessage);

            // Format message để staff nhận được
            Map<String, Object> staffMessage = new HashMap<>();
            staffMessage.put("type", "USER_CHAT");
            staffMessage.put("userId", userId);
            staffMessage.put("message", chatMessage);
            staffMessage.put("timestamp", LocalDateTime.now().toString());

            return staffMessage;

        } catch (Exception e) {
            log.error("Lỗi khi xử lý chat từ user: {}", e.getMessage(), e);
            Map<String, Object> errorMessage = new HashMap<>();
            errorMessage.put("type", "ERROR");
            errorMessage.put("message", "Lỗi gửi tin nhắn");
            return errorMessage;
        }
    }
}
