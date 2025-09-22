package com.foodorder.backend.order.service;

import com.foodorder.backend.order.config.PaymentConfig;
import com.foodorder.backend.order.entity.PaymentMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PaymentService {
    
    public String createPaymentUrl(PaymentMethod paymentMethod, String orderId, 
                                 long amount, String description, String returnUrl) {
        
        PaymentConfig config = PaymentConfig.getPaymentConfig(paymentMethod);
        
//        log.info("Creating payment URL for method: {}, bankCode: {}, embedData: {}",
//                paymentMethod, config.getBankCode(), config.getEmbedData());
//
        if (!config.requiresPaymentGateway()) {
            // COD - No payment URL needed
            return null;
        }
        
        if (config.isMoMoGateway()) {
            return createMoMoPaymentUrl(orderId, amount, description, returnUrl);
        }
        
        if (config.isZaloPayGateway()) {
            return createZaloPayPaymentUrl(orderId, amount, description, 
                                         returnUrl, config.getBankCode(), config.getEmbedData());
        }
        
        throw new IllegalArgumentException("Unsupported payment gateway: " + config.getGateway());
    }
    
    private String createMoMoPaymentUrl(String orderId, long amount, String description, String returnUrl) {
//        log.info("Creating MoMo payment URL for order: {}, amount: {}", orderId, amount);
        
        // MoMo API call here
        // Return the payment URL from MoMo response
        
        return "https://test-payment.momo.vn/v2/gateway/api/create";
    }
    
    private String createZaloPayPaymentUrl(String orderId, long amount, String description, 
                                         String returnUrl, String bankCode, String embedData) {
//        log.info("Creating ZaloPay payment URL for order: {}, amount: {}, bankCode: {}, embedData: {}",
//                orderId, amount, bankCode, embedData);

        return "https://sb-openapi.zalopay.vn/v2/create";
    }
    
    public PaymentConfig getPaymentConfig(PaymentMethod paymentMethod) {
        return PaymentConfig.getPaymentConfig(paymentMethod);
    }
}
