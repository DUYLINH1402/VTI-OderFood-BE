package com.foodorder.backend.payments.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request body để tạo yêu cầu thanh toán")
public class PaymentRequest {

    @Schema(description = "ID của đơn hàng cần thanh toán", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long orderId;

    @Schema(
        description = "Phương thức thanh toán",
        example = "ZALOPAY",
        allowableValues = {"ZALOPAY", "MOMO", "VISA", "ATM", "COD"}
    )
    private String paymentMethod;

    @Schema(description = "Mã ngân hàng (có thể null nếu khách không chọn trước)", example = "VCB")
    private String bankCode;

    @Schema(description = "Dữ liệu nhúng cho ATM và điểm thưởng", example = "{\"bankgroup\":\"ATM\"}")
    private String embedData;

    @Schema(description = "Số điểm sử dụng cho đơn hàng", example = "100")
    private Integer point;

}
