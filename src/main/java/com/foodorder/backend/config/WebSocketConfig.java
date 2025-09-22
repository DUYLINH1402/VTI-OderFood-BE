package com.foodorder.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Cấu hình WebSocket cho ứng dụng
 * Sử dụng STOMP protocol để giao tiếp real-time giữa client và server
 * Chủ yếu phục vụ cho tính năng StaffOrder - theo dõi đơn hàng real-time
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * Cấu hình message broker để định tuyến các message
     * - /topic: Dành cho broadcast message (nhiều client cùng lắng nghe)
     * - /queue: Dành cho point-to-point message (1-1)
     * - /app: Prefix cho các message từ client gửi lên server
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Kích hoạt simple broker cho các destination có prefix /topic và /queue
        config.enableSimpleBroker("/topic", "/queue", "/user");

        // Prefix cho messages từ client gửi tới server
        config.setApplicationDestinationPrefixes("/app");

    }

    /**
     * Đăng ký STOMP endpoints cho client kết nối
     * Client sẽ kết nối tới endpoint này để thiết lập WebSocket connection
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint chính cho WebSocket connection
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                        "https://oder-4c1f2.web.app",
                        "https://dongxanhfood.shop",
                        "http://localhost:5173",
                        "https://dongxanhfoodorder.shop"
                )
                .withSockJS(); // Enable SockJS fallback cho các browser không hỗ trợ WebSocket

        // Endpoint riêng cho staff orders (có thể phân quyền riêng sau này)
        registry.addEndpoint("/ws/staff-orders")
                .setAllowedOriginPatterns(
                        "https://oder-4c1f2.web.app",
                        "https://dongxanhfood.shop",
                        "http://localhost:5173",
                        "https://dongxanhfoodorder.shop"
                )
                .withSockJS();
    }
}
