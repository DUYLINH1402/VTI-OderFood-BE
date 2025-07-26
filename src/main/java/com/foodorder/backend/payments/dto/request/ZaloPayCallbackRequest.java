package com.foodorder.backend.payments.dto.request;

import lombok.Data;

@Data
public class ZaloPayCallbackRequest {
    // Fields theo tài liệu ZaloPay callback
    private String data; // JSON string chứa thông tin giao dịch
    private String mac; // Chữ ký để verify
    private Integer type; // Loại callback (1: Order, 2: Agreement)

    // Các field cũ để backward compatibility (nếu cần)
    private String app_id;
    private String app_trans_id;
    private String zp_trans_id;
    private String server_time;
    private String app_time;
    private String amount;
    private String item;
    private String embed_data;
    private String status;
    private String app_user;
}
