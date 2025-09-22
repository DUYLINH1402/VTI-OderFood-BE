package com.foodorder.backend.notifications.dto;

import com.foodorder.backend.notifications.entity.Notification;
import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO cho việc tạo thông báo mới
 * Chứa thông tin cần thiết để tạo một thông báo cho User hoặc Staff
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationCreateDTO {

    private Long userId; // Có thể null nếu là thông báo cho staff

    private Long orderId;

    @NotBlank(message = "Mã đơn hàng không được để trống")
    private String orderCode;

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @NotBlank(message = "Nội dung thông báo không được để trống")
    private String message;

    @NotBlank(message = "Loại thông báo không được để trống")
    private String type;

    // Thêm các trường mới cho Staff notifications
    @Builder.Default
    private Notification.RecipientType recipientType = Notification.RecipientType.USER;

    @NotNull(message = "ID người nhận không được để trống")
    private Long recipientId;
}
