package com.foodorder.backend.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO cho việc Admin hủy đơn hàng kèm lý do chi tiết
 * Lý do sẽ được lưu vào cột cancel_reason
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body để Admin hủy đơn hàng kèm lý do")
public class AdminCancelOrderRequest {

    @Schema(
        description = "Lý do hủy đơn hàng (tối đa 500 ký tự)",
        example = "Khách hàng yêu cầu hủy đơn",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Lý do hủy đơn không được để trống")
    @Size(max = 500, message = "Lý do hủy đơn không được vượt quá 500 ký tự")
    private String cancelReason;

    @Schema(
        description = "Ghi chú nội bộ bổ sung (chỉ Admin thấy, tối đa 2000 ký tự)",
        example = "Khách hàng gọi điện yêu cầu hủy lúc 10h sáng"
    )
    @Size(max = 2000, message = "Ghi chú nội bộ không được vượt quá 2000 ký tự")
    private String internalNote;
}

