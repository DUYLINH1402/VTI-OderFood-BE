package com.foodorder.backend.payments.service;


import com.foodorder.backend.payments.dto.request.PaymentRequest;
import com.foodorder.backend.payments.dto.response.PaymentResponse;

public interface PaymentService {
    PaymentResponse createOrder(PaymentRequest request);
}

