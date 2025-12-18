package com.foodorder.backend.websocket;

import com.foodorder.backend.security.JwtUtil;
import com.foodorder.backend.user.entity.User;
import com.foodorder.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket Controller xử lý các message real-time cho Staff
 * CHÚ Ý: Logic chat đã được chuyển sang ChatWebSocketController trong module chat
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class StaffWebSocketController {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    // Lưu trữ thông tin staff đang online (cho order management)
    private final Map<String, String> onlineStaff = new ConcurrentHashMap<>();

    /**
     * Staff đăng ký nhận thông báo order updates
     */
    @MessageMapping("/staff/register-order-updates")
    public void registerStaffOrderUpdates(@Payload Object payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            String token = extractTokenFromPayload(payload);

            if (token == null || token.trim().isEmpty()) {
                sendErrorToStaff("TOKEN_REQUIRED", "Vui lòng cung cấp token xác thực", headerAccessor.getSessionId());
                return;
            }

            if (!jwtUtil.validateToken(token)) {
                sendErrorToStaff("INVALID_TOKEN", "Token không hợp lệ", headerAccessor.getSessionId());
                return;
            }

            String username = jwtUtil.getUsernameFromToken(token);
            if (username == null || username.trim().isEmpty()) {
                sendErrorToStaff("INVALID_TOKEN", "Không thể lấy thông tin người dùng từ token", headerAccessor.getSessionId());
                return;
            }

            User staff = userService.findByUsername(username);

            if (staff == null) {
                sendErrorToStaff("USER_NOT_FOUND", "Không tìm thấy thông tin người dùng", headerAccessor.getSessionId());
                return;
            }

            String roleCode = staff.getRole().getCode();
            if (!"ROLE_STAFF".equals(roleCode) && !"ROLE_ADMIN".equals(roleCode)) {
                sendErrorToStaff("ACCESS_DENIED", "Bạn không có quyền truy cập chức năng này", headerAccessor.getSessionId());
                return;
            }

            String staffId = staff.getId().toString();
            Map<String, Object> sessionAttrs = headerAccessor.getSessionAttributes();
            if (sessionAttrs != null) {
                sessionAttrs.put("staffId", staffId);
                sessionAttrs.put("staffName", staff.getFullName());
                sessionAttrs.put("role", roleCode);
            }
            onlineStaff.put(headerAccessor.getSessionId(), staffId);

            Map<String, Object> welcomeMessage = new HashMap<>();
            welcomeMessage.put("type", "STAFF_ORDER_WELCOME");
            welcomeMessage.put("message", "Xin chào " + staff.getFullName() + "! Bạn đã kết nối thành công và sẵn sàng nhận thông báo đơn hàng.");
            welcomeMessage.put("staffId", staffId);
            welcomeMessage.put("staffName", staff.getFullName());
            welcomeMessage.put("role", roleCode);
            welcomeMessage.put("timestamp", LocalDateTime.now().toString());

            messagingTemplate.convertAndSend("/topic/staff-orders", welcomeMessage);

        } catch (Exception e) {
            log.error("Lỗi khi đăng ký staff order updates: {}", e.getMessage());
            sendErrorToStaff("REGISTRATION_ERROR", "Lỗi khi đăng ký", headerAccessor.getSessionId());
        }
    }

    /**
     * Staff đăng ký tổng quát (backward compatibility)
     */
    @MessageMapping("/staff/register")
    public void registerStaff(@Payload Object payload, SimpMessageHeaderAccessor headerAccessor) {
        // Redirect to order updates registration
        registerStaffOrderUpdates(payload, headerAccessor);
    }

    /**
     * Gửi thông báo order update tới tất cả staff
     */
    public void sendOrderUpdateToAllStaff(String orderId, String orderCode, String status, String message, String customerInfo) {
        try {
            Map<String, Object> orderUpdate = new HashMap<>();
            orderUpdate.put("type", "ORDER_UPDATE");
            orderUpdate.put("orderId", orderId);
            orderUpdate.put("orderCode", orderCode);
            orderUpdate.put("orderStatus", status);
            orderUpdate.put("message", message);
            orderUpdate.put("customerInfo", customerInfo);
            orderUpdate.put("timestamp", LocalDateTime.now().toString());

            messagingTemplate.convertAndSend("/topic/staff-orders", orderUpdate);
            log.info("Đã gửi thông báo order update tới tất cả staff: Order {}", orderCode);

        } catch (Exception e) {
            log.error("Lỗi khi gửi order update tới staff: {}", e.getMessage());
        }
    }

    /**
     * Gửi thông báo đơn hàng mới tới staff
     */
    public void sendNewOrderNotificationToStaff(String orderId, String orderCode, String customerName, String totalAmount) {
        try {
            Map<String, Object> newOrderNotification = new HashMap<>();
            newOrderNotification.put("type", "NEW_ORDER");
            newOrderNotification.put("orderId", orderId);
            newOrderNotification.put("orderCode", orderCode);
            newOrderNotification.put("customerName", customerName);
            newOrderNotification.put("totalAmount", totalAmount);
            newOrderNotification.put("message", "Có đơn hàng mới từ " + customerName);
            newOrderNotification.put("timestamp", LocalDateTime.now().toString());

            messagingTemplate.convertAndSend("/topic/staff-orders", newOrderNotification);
            log.info("Đã gửi thông báo đơn hàng mới tới staff: Order {}", orderCode);

        } catch (Exception e) {
            log.error("Lỗi khi gửi thông báo đơn hàng mới: {}", e.getMessage());
        }
    }

    /**
     * Xử lý khi có staff disconnect
     */
    public void handleStaffDisconnect(String sessionId) {
        try {
            String staffId = onlineStaff.remove(sessionId);
            if (staffId != null) {
                Map<String, Object> offlineMessage = new HashMap<>();
                offlineMessage.put("type", "STAFF_OFFLINE");
                offlineMessage.put("staffId", staffId);
                offlineMessage.put("timestamp", LocalDateTime.now().toString());

                messagingTemplate.convertAndSend("/topic/staff-orders", offlineMessage);
            }
        } catch (Exception e) {
            log.error("Lỗi khi xử lý staff disconnect: {}", e.getMessage());
        }
    }

    /**
     * Lấy số lượng staff đang online
     */
    public int getOnlineStaffCount() {
        return onlineStaff.size();
    }

    /**
     * Kiểm tra staff có đang online không
     */
    public boolean isStaffOnline(String staffId) {
        return onlineStaff.containsValue(staffId);
    }

    // ========== PRIVATE HELPER METHODS ==========

    /**
     * Extract token từ payload với nhiều format khác nhau
     */
    private String extractTokenFromPayload(Object payload) {
        try {
            if (payload instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> payloadMap = (Map<String, Object>) payload;
                Object tokenObj = payloadMap.get("token");
                return tokenObj != null ? tokenObj.toString() : null;
            }
            else if (payload instanceof String) {
                String payloadStr = (String) payload;
                if (payloadStr.startsWith("{") && payloadStr.endsWith("}")) {
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        @SuppressWarnings("unchecked")
                        Map<String, Object> parsedPayload = mapper.readValue(payloadStr, Map.class);
                        Object tokenObj = parsedPayload.get("token");
                        return tokenObj != null ? tokenObj.toString() : null;
                    } catch (Exception e) {
                        return payloadStr.trim();
                    }
                } else {
                    return payloadStr.trim();
                }
            }
            else if (payload instanceof byte[]) {
                try {
                    byte[] bytes = (byte[]) payload;
                    String jsonString = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);

                    if (jsonString.startsWith("{") && jsonString.endsWith("}")) {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        @SuppressWarnings("unchecked")
                        Map<String, Object> parsedPayload = mapper.readValue(jsonString, Map.class);
                        Object tokenObj = parsedPayload.get("token");
                        return tokenObj != null ? tokenObj.toString() : null;
                    } else {
                        return jsonString.trim();
                    }
                } catch (Exception e) {
                    return null;
                }
            }
            else {
                return payload.toString().trim();
            }
        } catch (Exception e) {
            log.error("Lỗi khi extract token từ payload: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Gửi thông báo lỗi tới staff
     */
    private void sendErrorToStaff(String errorCode, String errorMessage, String sessionId) {
        try {
            Map<String, Object> error = new HashMap<>();
            error.put("type", "ERROR");
            error.put("errorCode", errorCode);
            error.put("message", errorMessage);
            error.put("timestamp", LocalDateTime.now().toString());

            messagingTemplate.convertAndSend("/topic/staff-errors", error);
        } catch (Exception e) {
            log.error("Lỗi khi gửi error message: {}", e.getMessage());
        }
    }
}
