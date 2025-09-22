package com.foodorder.backend.payments.dto.request;

import lombok.Data;

@Data
public class PaymentRequest {
    private Long orderId;
    private String paymentMethod; // "ZALOPAY", "MOMO", "VISA", "ATM", "COD"
    private String bankCode; // Có thể null nếu khách không chọn trước
    private String embedData; // Dành cho ATM: {"bankgroup":"ATM"} và điểm thưởng
     private Integer point; // Số điểm sử dụng cho đơn hàng

}
