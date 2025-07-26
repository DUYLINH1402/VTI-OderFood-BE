package com.foodorder.backend.payments.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PaymentStatusResponse {
    private Long orderId;
    private String paymentStatus;
    private String orderStatus;
    private String paymentTransactionId;
    private LocalDateTime paymentTime;
}
