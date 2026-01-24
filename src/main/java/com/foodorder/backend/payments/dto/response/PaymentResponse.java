package com.foodorder.backend.payments.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Response chứa thông tin thanh toán")
public class PaymentResponse {

    @Schema(description = "URL thanh toán để redirect người dùng", example = "https://zalopay.vn/pay/...")
    private String paymentUrl;

    @Schema(description = "Cổng thanh toán", example = "ZALOPAY", allowableValues = {"ZALOPAY", "MOMO", "VNPAY"})
    private String paymentGateway;

    @Schema(description = "Trạng thái thanh toán", example = "PENDING", allowableValues = {"PENDING", "SUCCESS", "FAILED"})
    private String status;
}
