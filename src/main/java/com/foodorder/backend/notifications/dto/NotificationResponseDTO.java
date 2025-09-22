package com.foodorder.backend.notifications.dto;

import com.foodorder.backend.notifications.entity.Notification;
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
public class NotificationResponseDTO {

    private Long id;
    private Long userId;
    private Long orderId;
    private String orderCode;
    private String title;
    private String message;
    private String type;
    private Notification.RecipientType recipientType;
    private Long recipientId;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
