package com.foodorder.backend.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * WebSocket Event Listener xử lý các sự kiện kết nối và ngắt kết nối
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final StaffWebSocketController staffWebSocketController;

    /**
     * Xử lý khi có session kết nối
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        try {
            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
            String sessionId = headerAccessor.getSessionId();

        } catch (Exception e) {
            log.error("Lỗi khi xử lý WebSocket connect event: {}", e.getMessage(), e);
        }
    }

    /**
     * Xử lý khi có session ngắt kết nối
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        try {
            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
            String sessionId = headerAccessor.getSessionId();

            // Xử lý staff disconnect
            staffWebSocketController.handleStaffDisconnect(sessionId);

        } catch (Exception e) {
            log.error("Lỗi khi xử lý WebSocket disconnect event: {}", e.getMessage(), e);
        }
    }
}
