package com.foodorder.backend.order.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum định nghĩa các phương thức thanh toán
 */
@Getter
@AllArgsConstructor
public enum PaymentMethod {
    COD("COD", "Thanh toán khi nhận hàng"),
    ZALOPAY("ZALOPAY", "Thanh toán qua ZaloPay"),
    MOMO("MOMO", "Thanh toán qua MoMo"),
    VNPAY("VNPAY", "Thanh toán qua VNPay"),
    BANKING("BANKING", "Chuyển khoản ngân hàng"),
    ATM ("ATM", "Thanh toán qua thẻ ATM"),
    VISA ("CC", "Thanh toán qua thẻ Visa");
    private final String code;
    private final String description;
}
