package com.foodorder.backend.payments.controller;

import com.foodorder.backend.payments.dto.request.PaymentRequest;
import com.foodorder.backend.payments.dto.request.ZaloPayCallbackRequest;
import com.foodorder.backend.payments.dto.response.PaymentResponse;
import com.foodorder.backend.payments.service.impl.MomoPaymentService;
import com.foodorder.backend.payments.service.impl.ZaloPayPaymentService;
import com.foodorder.backend.coupons.service.CouponService;
import com.foodorder.backend.coupons.dto.request.ApplyCouponRequest;
import com.foodorder.backend.coupons.dto.response.CouponApplyResult;
import com.foodorder.backend.points.dto.response.PointsResponseDTO;
import com.foodorder.backend.points.service.PointsService;
import com.foodorder.backend.order.repository.OrderRepository;
import com.foodorder.backend.order.entity.Order;
import com.foodorder.backend.order.entity.PaymentStatus;
import com.foodorder.backend.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private ZaloPayPaymentService zaloPayService;
    @Autowired
    private MomoPaymentService momoPayService;
    @Autowired
    private CouponService couponService;
    @Autowired
    private PointsService pointsService;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;

    @PostMapping
    public PaymentResponse createPayment(@RequestBody PaymentRequest request) {
        // Validate trước khi tạo payment
        validatePaymentRequest(request);

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

    /**
     * Validate toàn bộ điều kiện trước khi tạo payment
     * - Order đã được validate coupon từ trước khi lưu
     * - Chỉ cần kiểm tra lại tính nhất quán
     */
    private void validatePaymentRequest(PaymentRequest request) {
        // Lấy thông tin order
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + request.getOrderId()));

        // 1. Kiểm tra trạng thái order
        if (order.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new IllegalArgumentException("Order payment status is not PENDING: " + order.getPaymentStatus());
        }

        // 2. Kiểm tra lại coupon nếu có (chỉ để đảm bảo tính nhất quán)
        if (order.getCouponCode() != null && !order.getCouponCode().trim().isEmpty()) {
            validateCouponConsistency(order);
        }

        // 3. Validate điểm thưởng nếu có sử dụng
        if (order.getDiscountAmount() != null && order.getDiscountAmount() > 0) {
            validatePointsForPayment(order);
        }

        System.out.println("Payment validation passed for order: " + order.getId());
    }

    /**
     * Kiểm tra tính nhất quán của coupon (Order đã validate từ trước)
     * Chỉ cần đảm bảo coupon vẫn hợp lệ tại thời điểm thanh toán
     */
    private void validateCouponConsistency(Order order) {
        try {
            // Kiểm tra nhanh coupon vẫn còn hợp lệ không
            ApplyCouponRequest couponRequest = new ApplyCouponRequest();
            couponRequest.setCouponCode(order.getCouponCode());
            couponRequest.setUserId(order.getUserId());
            couponRequest.setOrderAmount(order.getOriginalAmount() != null ?
                order.getOriginalAmount().doubleValue() : order.getTotalPrice().doubleValue());

            // Lấy danh sách foodIds từ order items
            if (order.getItems() != null && !order.getItems().isEmpty()) {
                List<Long> foodIds = order.getItems().stream()
                    .map(item -> item.getFoodId())
                    .collect(Collectors.toList());
                couponRequest.setFoodIds(foodIds);
            } else {
                couponRequest.setFoodIds(new ArrayList<>());
            }

            CouponApplyResult result = couponService.validateCouponForOrder(couponRequest);

            if (!Boolean.TRUE.equals(result.getSuccess())) {
                throw new IllegalArgumentException("Coupon không còn hợp lệ tại thời điểm thanh toán: " + result.getMessage());
            }

            // Kiểm tra số tiền giảm giá có khớp không (cho phép sai số nhỏ)
            if (order.getCouponDiscountAmount() != null && result.getDiscountAmount() != null) {
                double expectedDiscount = result.getDiscountAmount();
                double actualDiscount = order.getCouponDiscountAmount().doubleValue();

                if (Math.abs(expectedDiscount - actualDiscount) > 0.01) {
                    throw new IllegalArgumentException("Coupon discount amount mismatch. Expected: " +
                        expectedDiscount + ", Actual: " + actualDiscount);
                }
            }

            System.out.println("Coupon consistency check passed: " + order.getCouponCode());

        } catch (Exception e) {
            throw new IllegalArgumentException("Lỗi kiểm tra coupon: " + e.getMessage());
        }
    }

    /**
     * Validate điểm thưởng cho payment
     */
    private void validatePointsForPayment(Order order) {
        if (order.getUserId() == null) {
            throw new IllegalArgumentException("Guest user không thể sử dụng điểm thưởng");
        }
        try {
            // Lấy username từ userId
            String username = userRepository.findById(order.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user với id: " + order.getUserId()))
                .getUsername();
            // Lấy số điểm hiện tại của user
            PointsResponseDTO pointsDTO = pointsService.getCurrentPointsByUsername(username);
            int currentPoints = pointsDTO != null ? pointsDTO.getAvailablePoints() : 0;
            // Tính số điểm cần dùng (giả sử 1 điểm = 1000 VND)
            int pointsNeeded = order.getDiscountAmount() / 1000;
            if (currentPoints < pointsNeeded) {
                throw new IllegalArgumentException("Không đủ điểm thưởng. Cần " + pointsNeeded + " điểm, hiện có " + currentPoints + " điểm");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Lỗi kiểm tra điểm thưởng: " + e.getMessage());
        }
    }

    // Endpoint linh hoạt để handle ZaloPay callback với nhiều format
    @PostMapping("/zalopay/callback-flexible")
    public String handleZaloPayCallbackFlexible(@RequestBody Map<String, Object> payload) {
        // System.out.println("=== Received ZaloPay Callback ===");
        // System.out.println("Raw payload: " + payload);

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

            // System.out.println("Parsed callback - Data: " + callback.getData());
            // System.out.println("Parsed callback - MAC: " + callback.getMac());
            // System.out.println("Parsed callback - Type: " + callback.getType());

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
