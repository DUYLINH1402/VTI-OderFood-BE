package com.foodorder.backend.websocket;

import com.foodorder.backend.order.dto.OrderWebSocketMessage;
import com.foodorder.backend.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

/**
 * WebSocket Controller xử lý các message real-time cho StaffOrder
 * Nhận và xử lý các message từ client thông qua WebSocket
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class StaffWebSocketController {

    private final WebSocketService webSocketService;

    /**
     * Xử lý khi staff kết nối và đăng ký nhận thông báo
     * Client gửi message tới /app/staff/register
     */
    @MessageMapping("/staff/register")
    public void registerStaff(@Payload String staffInfo, SimpMessageHeaderAccessor headerAccessor) {
        try {
            // Lưu thông tin staff vào session để phân quyền
            headerAccessor.getSessionAttributes().put("staffId", staffInfo);
//
        } catch (Exception e) {
            log.error("Lỗi khi đăng ký staff: {}", e.getMessage());
        }
    }

    /**
     * Xử lý khi staff yêu cầu thông tin chi tiết đơn hàng
     * Client gửi message tới /app/staff/get-order-details
     */
    @MessageMapping("/staff/get-order-details")
    @SendTo("/topic/order-details")
    public String getOrderDetails(@Payload Long orderId) {
        try {

            // Trả về thông tin chi tiết đơn hàng (có thể call service để lấy data)
            return "Chi tiết đơn hàng " + orderId + " đã được gửi";

        } catch (Exception e) {
            log.error("Lỗi khi lấy chi tiết đơn hàng: {}", e.getMessage());
            return "Lỗi khi lấy thông tin đơn hàng";
        }
    }


    /**
     * Ping/Pong để kiểm tra kết nối
     * Client gửi message tới /app/ping
     */
    @MessageMapping("/ping")
    @SendTo("/topic/pong")
    public String ping() {
        return "pong - " + System.currentTimeMillis();
    }
}





