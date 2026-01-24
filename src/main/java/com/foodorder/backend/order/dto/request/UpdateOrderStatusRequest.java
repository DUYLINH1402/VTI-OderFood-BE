package com.foodorder.backend.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho request cập nhật trạng thái đơn hàng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body để cập nhật trạng thái đơn hàng")
public class UpdateOrderStatusRequest {

    @Schema(
        description = "Trạng thái mới của đơn hàng",
        example = "CONFIRMED",
        requiredMode = Schema.RequiredMode.REQUIRED,
        allowableValues = {"PENDING", "CONFIRMED", "PREPARING", "READY", "SHIPPING", "DELIVERED", "CANCELLED"}
    )
    @NotBlank(message = "Trạng thái đơn hàng không được để trống")
    private String status;

    @Schema(description = "Ghi chú cho thay đổi trạng thái", example = "Đã xác nhận đơn hàng qua điện thoại")
    private String note;
}
