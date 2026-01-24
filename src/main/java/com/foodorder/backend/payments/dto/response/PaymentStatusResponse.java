package com.foodorder.backend.payments.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "Response chứa trạng thái thanh toán của đơn hàng")
public class PaymentStatusResponse {

    @Schema(description = "ID của đơn hàng", example = "100")
    private Long orderId;

    @Schema(description = "Trạng thái thanh toán", example = "PAID", allowableValues = {"PENDING", "PAID", "FAILED", "REFUNDED"})
    private String paymentStatus;

    @Schema(description = "Trạng thái đơn hàng", example = "CONFIRMED")
    private String orderStatus;

    @Schema(description = "Mã giao dịch thanh toán", example = "TXN123456789")
    private String paymentTransactionId;

    @Schema(description = "Thời gian thanh toán", example = "2025-01-20T10:32:00")
    private LocalDateTime paymentTime;
}
