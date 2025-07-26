package com.foodorder.backend.order.util;

import com.foodorder.backend.order.entity.PaymentMethod;

public class PaymentConfigHelper {
    
    public static String getBankCode(PaymentMethod paymentMethod) {
        switch (paymentMethod) {
            case ZALOPAY:
                return "zalopayapp";
            case ATM:
                return "";
            case VISA:
                return "CC";
            case MOMO:
            case COD:
            default:
                return "";
        }
    }
    
    public static String getEmbedData(PaymentMethod paymentMethod) {
        switch (paymentMethod) {
            case ATM:
                return "{\"bankgroup\":\"ATM\"}";
            case ZALOPAY:
            case VISA:
            case MOMO:
            case COD:
            default:
                return "";
        }
    }
}
