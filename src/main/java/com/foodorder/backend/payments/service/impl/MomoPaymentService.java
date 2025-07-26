package com.foodorder.backend.payments.service.impl;

import com.foodorder.backend.payments.dto.request.PaymentRequest;
import com.foodorder.backend.payments.dto.response.PaymentResponse;
import com.foodorder.backend.payments.service.PaymentService;
import org.springframework.stereotype.Service;

@Service
public class MomoPaymentService implements PaymentService {

    @Override
    public PaymentResponse createOrder(PaymentRequest request) {
        // Logic thực tế sẽ bổ sung sau
        PaymentResponse res = new PaymentResponse();
        res.setPaymentGateway("MOMO");
        res.setStatus("PENDING");
        res.setPaymentUrl("https://momo.vn/payment-demo-url");
        return res;
    }

    public String handleCallback(Object callback) {
        // Tạm thời return "OK", logic xử lý callback bổ sung sau
        return "OK";
    }
}

