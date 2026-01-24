package com.foodorder.backend.notifications.dto;

import com.foodorder.backend.notifications.entity.Notification;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request body để tạo thông báo mới")
public class NotificationCreateDTO {

    @Schema(description = "ID của user nhận thông báo (null nếu gửi cho staff)", example = "1")
    private Long userId;

    @Schema(description = "ID đơn hàng liên quan", example = "100")
    private Long orderId;

    @Schema(
        description = "Mã đơn hàng",
        example = "ORD-20250120-001",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Mã đơn hàng không được để trống")
    private String orderCode;

    @Schema(
        description = "Tiêu đề thông báo",
        example = "Đơn hàng đã được xác nhận",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @Schema(
        description = "Nội dung thông báo",
        example = "Đơn hàng ORD-20250120-001 của bạn đã được xác nhận và đang được chuẩn bị",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Nội dung thông báo không được để trống")
    private String message;

    @Schema(
        description = "Loại thông báo",
        example = "ORDER_CONFIRMED",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Loại thông báo không được để trống")
    private String type;

    @Schema(description = "Loại người nhận", example = "USER", allowableValues = {"USER", "STAFF"})
    @Builder.Default
    private Notification.RecipientType recipientType = Notification.RecipientType.USER;

    @Schema(
        description = "ID của người nhận",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "ID người nhận không được để trống")
    private Long recipientId;
}
