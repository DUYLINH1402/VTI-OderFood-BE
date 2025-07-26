package com.foodorder.backend.payments.controller;

import com.foodorder.backend.payments.dto.request.PaymentRequest;
import com.foodorder.backend.payments.dto.request.ZaloPayCallbackRequest;
import com.foodorder.backend.payments.dto.response.PaymentResponse;
import com.foodorder.backend.payments.service.impl.MomoPaymentService;
import com.foodorder.backend.payments.service.impl.ZaloPayPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private ZaloPayPaymentService zaloPayService;
    @Autowired
    private MomoPaymentService momoPayService;

    @PostMapping
    public PaymentResponse createPayment(@RequestBody PaymentRequest request) {
        switch (request.getPaymentMethod().toUpperCase()) {
            case "ZALOPAY":
            case "ATM":
            case "VISA":
                // Tất cả đều sử dụng ZaloPay gateway nhưng với bankCode khác nhau
                return zaloPayService.createOrder(request);
            case "MOMO":
                return momoPayService.createOrder(request);
            case "COD":
                // COD không cần payment gateway
                PaymentResponse codResponse = new PaymentResponse();
                codResponse.setStatus("SUCCESS"); // Success
                codResponse.setPaymentGateway("COD");
                codResponse.setPaymentUrl(""); // Empty URL for COD
                return codResponse;
            default:
                throw new IllegalArgumentException("Invalid payment method: " + request.getPaymentMethod());
        }
    }

    // Endpoint linh hoạt để handle ZaloPay callback với nhiều format
    @PostMapping("/zalopay/callback-flexible")
    public String handleZaloPayCallbackFlexible(@RequestBody Map<String, Object> payload) {
        System.out.println("=== Received ZaloPay Callback ===");
        System.out.println("Raw payload: " + payload);

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    false);

            // Parse payload theo format mới của ZaloPay
            ZaloPayCallbackRequest callback = new ZaloPayCallbackRequest();

            // Set các field chính từ payload
            if (payload.containsKey("data")) {
                callback.setData((String) payload.get("data"));
            }
            if (payload.containsKey("mac")) {
                callback.setMac((String) payload.get("mac"));
            }
            if (payload.containsKey("type")) {
                callback.setType((Integer) payload.get("type"));
            }

            System.out.println("Parsed callback - Data: " + callback.getData());
            System.out.println("Parsed callback - MAC: " + callback.getMac());
            System.out.println("Parsed callback - Type: " + callback.getType());

            if (callback.getData() != null && callback.getMac() != null) {
                return zaloPayService.handleCallback(callback);
            } else {
                System.err.println("Missing required fields: data or mac");
                return "OK"; // Vẫn return OK để tránh retry
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "OK"; // Return OK để tránh ZaloPay retry
        }
    }

    // Endpoint để manual check payment status
    @GetMapping("/zalopay/check-status/{appTransId}")
    public String checkPaymentStatus(@PathVariable String appTransId) {
        try {
            // Query từ ZaloPay API
            zaloPayService.queryPaymentStatus(appTransId);
            return "Payment status check completed for: " + appTransId;
        } catch (Exception e) {
            return "Error checking payment status: " + e.getMessage();
        }
    }

    // Endpoint để frontend update payment status (cho trường hợp thất bại)
    @PostMapping("/update-status")
    public ResponseEntity<?> updatePaymentStatus(@RequestBody Map<String, Object> request) {
        try {
            String appTransId = (String) request.get("appTransId");
            String status = (String) request.get("status"); // "SUCCESS" hoặc "FAILED"
            String errorCode = (String) request.get("errorCode"); // Mã lỗi từ ZaloPay
            String errorMessage = (String) request.get("errorMessage"); // Thông báo lỗi

            System.out.println("=== Frontend Update Payment Status ===");
            System.out.println("App Trans ID: " + appTransId);
            System.out.println("Status: " + status);
            System.out.println("Error Code: " + errorCode);
            System.out.println("Error Message: " + errorMessage);

            if (appTransId == null || status == null) {
                return ResponseEntity.badRequest().body("Missing required fields");
            }

            // Parse orderId từ appTransId
            String[] parts = appTransId.split("_");
            if (parts.length != 2) {
                return ResponseEntity.badRequest().body("Invalid appTransId format");
            }
            Long orderId = Long.parseLong(parts[1]);

            // Update payment status dựa trên kết quả từ frontend
            if ("FAILED".equals(status)) {
                zaloPayService.updatePaymentStatusFromFrontend(orderId, null, "FAILED");
                return ResponseEntity.ok().body("Payment status updated to FAILED");
            } else if ("SUCCESS".equals(status)) {
                // Trường hợp này ít xảy ra vì callback đã xử lý, nhưng để phòng trường hợp
                zaloPayService.updatePaymentStatusFromFrontend(orderId, appTransId, "PAID");
                return ResponseEntity.ok().body("Payment status updated to PAID");
            } else {
                return ResponseEntity.badRequest().body("Invalid status: " + status);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error updating payment status: " + e.getMessage());
        }
    }

}
