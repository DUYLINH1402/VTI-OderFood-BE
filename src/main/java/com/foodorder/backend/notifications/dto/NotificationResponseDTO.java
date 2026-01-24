package com.foodorder.backend.notifications.dto;

import com.foodorder.backend.notifications.entity.Notification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO cho response thông báo
 * Chứa thông tin thông báo trả về cho client (User/Staff)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response chứa thông tin thông báo")
public class NotificationResponseDTO {

    @Schema(description = "ID của thông báo", example = "1")
    private Long id;

    @Schema(description = "ID của user nhận thông báo", example = "1")
    private Long userId;

    @Schema(description = "ID đơn hàng liên quan", example = "100")
    private Long orderId;

    @Schema(description = "Mã đơn hàng", example = "ORD-20250120-001")
    private String orderCode;

    @Schema(description = "Tiêu đề thông báo", example = "Đơn hàng đã được xác nhận")
    private String title;

    @Schema(description = "Nội dung thông báo", example = "Đơn hàng của bạn đã được xác nhận")
    private String message;

    @Schema(description = "Loại thông báo", example = "ORDER_CONFIRMED")
    private String type;

    @Schema(description = "Loại người nhận", example = "USER", allowableValues = {"USER", "STAFF"})
    private Notification.RecipientType recipientType;

    @Schema(description = "ID của người nhận", example = "1")
    private Long recipientId;

    @Schema(description = "Đã đọc chưa", example = "false")
    private Boolean isRead;

    @Schema(description = "Thời gian tạo thông báo", example = "2025-01-20T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Thời gian đọc thông báo", example = "2025-01-20T10:35:00")
    private LocalDateTime readAt;
}
