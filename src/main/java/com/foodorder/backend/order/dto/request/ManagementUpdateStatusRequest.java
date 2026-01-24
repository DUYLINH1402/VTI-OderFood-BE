package com.foodorder.backend.order.dto.request;

import com.foodorder.backend.order.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO cho request cập nhật trạng thái đơn hàng bởi Staff/Admin
 */
@Data
@Schema(description = "Request body để Staff/Admin cập nhật trạng thái đơn hàng")
public class ManagementUpdateStatusRequest {
    
    @Schema(
        description = "Trạng thái mới của đơn hàng",
        example = "CONFIRMED",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Trạng thái không được để trống")
    private OrderStatus status;
    
    @Schema(description = "Ghi chú cho khách hàng", example = "Đơn hàng đang được chuẩn bị")
    private String note;

    @Schema(description = "Ghi chú nội bộ (chỉ Staff/Admin thấy)", example = "Đã xác nhận với khách qua điện thoại")
    private String internalNote;

    @Schema(description = "Có thông báo cho khách hàng không", example = "true", defaultValue = "true")
    private boolean notifyCustomer = true;
}
