package com.foodorder.backend.websocket;

import com.foodorder.backend.order.dto.OrderWebSocketMessage;
import com.foodorder.backend.security.JwtUtil;
import com.foodorder.backend.service.WebSocketService;
import com.foodorder.backend.user.entity.User;
import com.foodorder.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket Controller xử lý các message real-time cho User
 * CHÚ Ý: Logic chat đã được chuyển sang ChatWebSocketController trong module chat
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class UserWebSocketController {

    private final WebSocketService webSocketService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * Xử lý khi user đăng ký kênh order-updates
     */
    @MessageMapping("/user/{userId}/register-order-updates")
    public void registerOrderUpdates(@DestinationVariable String userId,
                                   @Payload(required = false) String message,
                                   SimpMessageHeaderAccessor headerAccessor) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return;
            }

            Map<String, Object> sessionAttrs = headerAccessor.getSessionAttributes();
            if (sessionAttrs != null) {
                sessionAttrs.put("userId", userId);
            }
            String normalizedUserId = userId.trim();

            Map<String, Object> welcomeMessage = new HashMap<>();
            welcomeMessage.put("type", "WELCOME");
            welcomeMessage.put("message", "Bạn đã kết nối thành công! Sẵn sàng nhận thông báo đơn hàng.");
            welcomeMessage.put("userId", normalizedUserId);
            welcomeMessage.put("timestamp", LocalDateTime.now().toString());

            messagingTemplate.convertAndSendToUser(
                normalizedUserId,
                "/queue/order-updates",
                welcomeMessage
            );

        } catch (Exception e) {
            log.error("Lỗi khi đăng ký order-updates cho user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Xử lý khi user đăng ký WebSocket tổng quát
     */
    @MessageMapping("/user/register")
    public void registerUser(@Payload String userId, SimpMessageHeaderAccessor headerAccessor) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return;
            }

            Map<String, Object> sessionAttrs = headerAccessor.getSessionAttributes();
            if (sessionAttrs != null) {
                sessionAttrs.put("userId", userId);
            }
            String normalizedUserId = userId.trim();

            Map<String, Object> welcomeMessage = new HashMap<>();
            welcomeMessage.put("type", "WELCOME");
            welcomeMessage.put("message", "Bạn đã kết nối thành công! Sẵn sàng nhận thông báo đơn hàng.");
            welcomeMessage.put("userId", normalizedUserId);
            welcomeMessage.put("timestamp", LocalDateTime.now().toString());
            welcomeMessage.put("channel", "/user/" + normalizedUserId + "/queue/order-updates");

            messagingTemplate.convertAndSendToUser(
                normalizedUserId,
                "/queue/order-updates",
                welcomeMessage
            );

        } catch (Exception e) {
            log.error("Lỗi khi đăng ký user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Gửi order update tới user cụ thể
     */
    public void sendOrderUpdateToUser(String userId, String orderId, String status, String message) {
        try {
            OrderWebSocketMessage updateMessage = OrderWebSocketMessage.builder()
                    .orderId(Long.parseLong(orderId))
                    .orderCode(orderId)
                    .orderStatus(status)
                    .message(message)
                    .customerId(Long.parseLong(userId))
                    .timestamp(System.currentTimeMillis())
                    .build();

            webSocketService.sendNotificationToUser(Long.parseLong(userId), updateMessage);

        } catch (Exception e) {
            log.error("Lỗi khi gửi order update tới user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Gửi thông báo lỗi tới user
     */
    private void sendErrorToUser(String errorCode, String errorMessage, String sessionId) {
        try {
            Map<String, Object> error = new HashMap<>();
            error.put("type", "ERROR");
            error.put("errorCode", errorCode);
            error.put("message", errorMessage);
            error.put("timestamp", LocalDateTime.now().toString());

            messagingTemplate.convertAndSend("/topic/user-errors", error);
        } catch (Exception e) {
            log.error("Lỗi khi gửi error message tới user: {}", e.getMessage());
        }
    }
}
