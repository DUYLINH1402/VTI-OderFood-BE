package com.foodorder.backend.payments.dto.response;

import lombok.Data;

@Data
public class PaymentResponse {
    private String paymentUrl; // Link trả về cho FE để redirect
    private String paymentGateway; // "ZALOPAY", "MOMO"
    private String status; // "PENDING" v.v
}
