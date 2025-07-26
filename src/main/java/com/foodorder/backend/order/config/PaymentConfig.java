package com.foodorder.backend.order.config;

import com.foodorder.backend.order.entity.PaymentMethod;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentConfig {
    private String bankCode;
    private String embedData;
    private String gateway; // "ZALOPAY", "MOMO", "NONE"
    
    public static PaymentConfig getPaymentConfig(PaymentMethod paymentMethod) {
        switch (paymentMethod) {
            case COD:
                return new PaymentConfig("", "", "NONE");
                
            case MOMO:
                return new PaymentConfig("", "", "MOMO");
                
            case ZALOPAY:
                return new PaymentConfig("zalopayapp", "", "ZALOPAY");
                
            case ATM:
                return new PaymentConfig("", "{\"bankgroup\":\"ATM\"}", "ATM");
                
            case VISA:
                return new PaymentConfig("CC", "", "VISA");
                
            default:
                throw new IllegalArgumentException("Unsupported payment method: " + paymentMethod);
        }
    }
    
    public boolean requiresPaymentGateway() {
        return !gateway.equals("NONE");
    }
    
    public boolean isZaloPayGateway() {
        return gateway.equals("ZALOPAY");
    }
    
    public boolean isMoMoGateway() {
        return gateway.equals("MOMO");
    }
}
