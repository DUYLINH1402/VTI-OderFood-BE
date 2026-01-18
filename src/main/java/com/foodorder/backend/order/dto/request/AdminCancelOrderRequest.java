package com.foodorder.backend.order.dto.request;

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
public class AdminCancelOrderRequest {

    @NotBlank(message = "Lý do hủy đơn không được để trống")
    @Size(max = 500, message = "Lý do hủy đơn không được vượt quá 500 ký tự")
    private String cancelReason;

    @Size(max = 2000, message = "Ghi chú nội bộ không được vượt quá 2000 ký tự")
    private String internalNote; // Ghi chú nội bộ bổ sung (optional)
}

