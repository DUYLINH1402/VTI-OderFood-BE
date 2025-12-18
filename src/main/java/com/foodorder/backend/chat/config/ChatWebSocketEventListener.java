package com.foodorder.backend.chat.config;

import com.foodorder.backend.chat.controller.ChatWebSocketController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * WebSocket Event Listener cho Chat
 * Xử lý connect/disconnect events để quản lý trạng thái online/offline
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketEventListener {

    private final ChatWebSocketController chatWebSocketController;

    /**
     * Xử lý khi có WebSocket connection được thiết lập
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

//        log.info("WebSocket connection established. Session ID: {}", sessionId);
    }

    /**
     * Xử lý khi có WebSocket connection bị disconnect
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

//        log.info("WebSocket connection closed. Session ID: {}", sessionId);

        // Gọi ChatWebSocketController để xử lý disconnect
        if (sessionId != null) {
            chatWebSocketController.handleDisconnect(sessionId);
        }
    }
}
