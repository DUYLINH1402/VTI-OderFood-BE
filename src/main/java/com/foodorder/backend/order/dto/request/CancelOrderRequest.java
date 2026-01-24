package com.foodorder.backend.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body để khách hàng hủy đơn hàng")
public class CancelOrderRequest {

    @Schema(
        description = "Lý do hủy đơn hàng",
        example = "Tôi đổi ý không muốn đặt nữa"
    )
    private String cancelReason;
}
