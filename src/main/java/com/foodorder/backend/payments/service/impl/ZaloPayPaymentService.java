package com.foodorder.backend.payments.service.impl;

import com.foodorder.backend.order.entity.Order;
import com.foodorder.backend.order.entity.OrderItem;
import com.foodorder.backend.order.entity.OrderStatus;
import com.foodorder.backend.order.entity.OrderTracking;
import com.foodorder.backend.order.entity.OrderTrackingStatus;
import com.foodorder.backend.order.entity.PaymentStatus;
import com.foodorder.backend.order.repository.OrderRepository;
import com.foodorder.backend.order.repository.OrderItemRepository;
import com.foodorder.backend.order.repository.OrderTrackingRepository;
import com.foodorder.backend.payments.dto.request.PaymentRequest;
import com.foodorder.backend.payments.dto.request.ZaloPayCallbackRequest;
import com.foodorder.backend.payments.dto.response.PaymentResponse;
import com.foodorder.backend.payments.dto.response.PaymentStatusResponse;
import com.foodorder.backend.payments.service.PaymentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ZaloPayPaymentService extends BasePaymentService implements PaymentService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OrderTrackingRepository orderTrackingRepository;

    @Value("${zalopay.app-id}")
    private Integer appId;

    @Value("${zalopay.key1}")
    private String key1;

    @Value("${zalopay.key2}")
    private String key2;

    @Value("${zalopay.endpoint}")
    private String endpoint;

    @Value("${zalopay.callback-url}")
    private String callbackUrl;

    // Constructor để gọi super()
    public ZaloPayPaymentService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
            OrderTrackingRepository orderTrackingRepository) {
        super(orderRepository, orderItemRepository);
        this.orderTrackingRepository = orderTrackingRepository;
    }

    @Override
    public PaymentResponse createOrder(PaymentRequest request) {

        try {
            // 1. Lấy order nội bộ từ DB với items
            Order order = getOrderWithItems(request.getOrderId());

            // 2. Chuẩn bị thông tin cho ZaloPay
            String appTransId = genAppTransId(order.getId());
            String appUser = order.getUserId() != null ? String.valueOf(order.getUserId()) : "guest";
            long appTime = System.currentTimeMillis();
            long amount = order.getTotalPrice() != null ? order.getTotalPrice().intValue() : 0; // Đơn vị VND

            // Build items JSON - Sử dụng logic chung từ BasePaymentService
            List<OrderItem> orderItems = getOrderItems(order);

            List<Map<String, Object>> items = new ArrayList<>();
            for (OrderItem item : orderItems) {
                if (item.getFood() == null) {
                    throw new IllegalStateException("OrderItem food is null for item id: " + item.getId());
                }
                Map<String, Object> food = new HashMap<>();
                food.put("itemid", item.getFood().getId());
                food.put("itemname", item.getFood().getName());
                food.put("itemprice", item.getPrice() != null ? item.getPrice().intValue() : 0);
                food.put("itemquantity", item.getQuantity() != null ? item.getQuantity() : 0);
                items.add(food);
            }

            String itemsJson;
            try {
                itemsJson = objectMapper.writeValueAsString(items);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to convert items to JSON", e);
            }

            // Embed data (redirect về trang thank you)
            String redirectUrl = "http://localhost:5173/thanh-toan/ket-qua?appTransId=" + appTransId;
            Map<String, Object> embedData = new HashMap<>();
            embedData.put("redirecturl", redirectUrl);

            // Thêm embedData từ PaymentRequest nếu có (dành cho ATM)
            if (request.getEmbedData() != null && !request.getEmbedData().isEmpty()) {
                try {
                    // Parse embedData từ request (JSON string) và merge vào embedData hiện tại
                    @SuppressWarnings("unchecked")
                    Map<String, Object> additionalEmbedData = objectMapper.readValue(request.getEmbedData(), Map.class);
                    embedData.putAll(additionalEmbedData);
                } catch (JsonProcessingException e) {
                    System.err.println("Failed to parse embedData from request: " + e.getMessage());
                    // Nếu parse lỗi, vẫn tiếp tục với embedData mặc định
                }
            }

            String embedDataJson = objectMapper.writeValueAsString(embedData);

            // 3. Build MAC (chuỗi ký hash) - theo thứ tự đúng của ZaloPay
            String data = appId + "|" + appTransId + "|" + appUser + "|" + amount + "|" +
                    appTime + "|" + embedDataJson + "|" + itemsJson;

            HmacUtils hmacUtils = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, key1);
            String mac = hmacUtils.hmacHex(data);

            // 4. Build request body (as JSON string to keep item/embed_data as string)
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("app_id", appId);
            body.put("app_trans_id", appTransId);
            body.put("app_user", appUser);
            body.put("app_time", appTime);
            body.put("amount", amount);
            body.put("item", itemsJson); // giữ nguyên là string
            body.put("embed_data", embedDataJson); // giữ nguyên là string
            body.put("description", "Thanh toán đơn hàng #" + order.getId());
            body.put("bank_code", request.getBankCode() != null ? request.getBankCode() : "");
            body.put("callback_url", callbackUrl);
            body.put("mac", mac);

            // Serialize toàn bộ body thành JSON string
            String bodyJson = objectMapper.writeValueAsString(body);

            // 5. Gọi API ZaloPay (RestTemplate hoặc WebClient)
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> httpEntity = new HttpEntity<>(bodyJson, headers);

            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> resp = restTemplate.postForEntity(endpoint, httpEntity, Map.class);

            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                throw new RuntimeException("Call ZaloPay API failed");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> respBody = resp.getBody();
            if (respBody == null) {
                throw new RuntimeException("Empty response from ZaloPay API");
            }
            String orderUrl = (String) respBody.get("order_url"); // Link để redirect khách sang ZaloPay

            // 6. Return về FE
            PaymentResponse paymentResponse = new PaymentResponse();
            paymentResponse.setPaymentUrl(orderUrl);
            paymentResponse.setPaymentGateway("ZALOPAY");
            paymentResponse.setStatus("PENDING");
            return paymentResponse;

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to process JSON", e);
        }
    }

    private String genAppTransId(Long orderId) {
        // Format theo yêu cầu ZaloPay: yymmdd_xxxxxxxxx
        String date = new SimpleDateFormat("yyMMdd").format(new Date());
        return date + "_" + orderId;
    }

    public String handleCallback(ZaloPayCallbackRequest callback) {
        try {
            // Kiểm tra các field bắt buộc
            if (callback.getData() == null || callback.getMac() == null) {
                return "OK"; // Vẫn return OK để không bị retry
            }

            // Verify MAC signature với key2
            String dataStr = callback.getData();
            HmacUtils hmacUtils = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, key2);
            String expectedMac = hmacUtils.hmacHex(dataStr);


            if (!expectedMac.equals(callback.getMac())) {
                System.err.println("Invalid MAC signature. Expected: " + expectedMac + ", Got: " + callback.getMac());
                return "OK"; // Vẫn return OK để không bị retry
            }

            // Parse JSON data từ callback
            @SuppressWarnings("unchecked")
            Map<String, Object> callbackData = objectMapper.readValue(dataStr, Map.class);

            String appTransId = (String) callbackData.get("app_trans_id");
            Object zpTransIdObj = callbackData.get("zp_trans_id");
            String zpTransId = zpTransIdObj != null ? String.valueOf(zpTransIdObj) : null;

            if (appTransId == null) {
                System.err.println("Missing app_trans_id in callback data");
                return "OK";
            }

            // Parse orderId từ app_trans_id (format: yymmdd_orderId)
            String[] parts = appTransId.split("_");
            if (parts.length != 2) {
                System.err.println("Invalid app_trans_id format: " + appTransId);
                return "OK";
            }
            Long orderId = Long.parseLong(parts[1]);

            // ZaloPay chỉ gửi callback khi thanh toán THÀNH CÔNG
            // Nên chúng ta mặc định đây là thanh toán thành công
            updateOrderPaymentStatus(orderId, zpTransId, "PAID");

            return "OK"; // Phải return "OK" để ZaloPay biết đã nhận callback thành công

        } catch (Exception e) {
            // Log error nhưng vẫn return OK để tránh ZaloPay retry
            System.err.println("Error processing callback: " + e.getMessage());
            e.printStackTrace();
            return "OK";
        }
    }

    private void updateOrderPaymentStatus(Long orderId, String transactionId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // Cập nhật payment status và order status
        if ("PAID".equals(status)) {
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setPaymentTime(LocalDateTime.now());
            order.setPaymentTransactionId(transactionId);

            // Cập nhật order status thành PROCESSING khi thanh toán thành công
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.PROCESSING);
            }

            // Thêm OrderTracking record với status PAID
            OrderTracking tracking = OrderTracking.builder()
                    .orderId(orderId)
                    .status(OrderTrackingStatus.PAID)
                    .changedAt(LocalDateTime.now())
                    .build();
            orderTrackingRepository.save(tracking);

        } else if ("FAILED".equals(status)) {
            order.setPaymentStatus(PaymentStatus.FAILED);
            order.setPaymentTransactionId(transactionId);

            // Cập nhật order status thành CANCELLED khi thanh toán thất bại
            if (order.getStatus() == OrderStatus.PENDING) {
                order.setStatus(OrderStatus.CANCELLED);
            }

            // Thêm OrderTracking record với status FAILED
            OrderTracking tracking = OrderTracking.builder()
                    .orderId(orderId)
                    .status(OrderTrackingStatus.FAILED)
                    .changedAt(LocalDateTime.now())
                    .build();
            orderTrackingRepository.save(tracking);
        }

        orderRepository.save(order);
    }

    /**
     * Public method để update payment status từ frontend
     */
    public void updatePaymentStatusFromFrontend(Long orderId, String transactionId, String status) {
        updateOrderPaymentStatus(orderId, transactionId, status);
    }

    /**
     * Query payment status from ZaloPay API
     */
    public PaymentStatusResponse queryPaymentStatus(String appTransId) {
        try {
            // Build query request
            String data = appId + "|" + appTransId + "|" + key1;
            HmacUtils hmacUtils = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, key1);
            String mac = hmacUtils.hmacHex(data);

            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("app_id", appId);
            queryParams.put("app_trans_id", appTransId);
            queryParams.put("mac", mac);

            // Call ZaloPay query API (thường là endpoint khác)
            String queryEndpoint = endpoint.replace("/create", "/query");

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String queryJson = objectMapper.writeValueAsString(queryParams);
            HttpEntity<String> httpEntity = new HttpEntity<>(queryJson, headers);

            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> resp = restTemplate.postForEntity(queryEndpoint, httpEntity, Map.class);

            if (resp.getBody() != null) {
                // Parse result và return PaymentStatusResponse
                PaymentStatusResponse response = new PaymentStatusResponse();
                // TODO: Implement logic dựa trên response từ ZaloPay
                return response;
            }

            throw new RuntimeException("Failed to query payment status from ZaloPay");

        } catch (Exception e) {
            throw new RuntimeException("Error querying payment status: " + e.getMessage(), e);
        }
    }
}
