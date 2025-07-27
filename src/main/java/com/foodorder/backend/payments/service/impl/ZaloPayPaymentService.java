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
import com.foodorder.backend.points.repository.RewardPointRepository;
import com.foodorder.backend.points.repository.PointHistoryRepository;
import com.foodorder.backend.points.service.PointsService;
import com.foodorder.backend.user.entity.User;
import com.foodorder.backend.user.repository.UserRepository;
import com.foodorder.backend.util.VnCurrencyFormatter;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import com.foodorder.backend.service.BrevoEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ZaloPayPaymentService extends BasePaymentService implements PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(ZaloPayPaymentService.class);

    private final UserRepository userRepository;
    private final RewardPointRepository rewardPointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final com.foodorder.backend.points.service.PointsService pointsService;
    private final BrevoEmailService brevoEmailService;
    private final TemplateEngine templateEngine;

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
            OrderTrackingRepository orderTrackingRepository,
            RewardPointRepository rewardPointRepository,
            PointHistoryRepository pointHistoryRepository,
            UserRepository userRepository,
            PointsService pointsService,
            BrevoEmailService brevoEmailService,
            TemplateEngine templateEngine) {
        super(orderRepository, orderItemRepository);
        this.orderTrackingRepository = orderTrackingRepository;
        this.rewardPointRepository = rewardPointRepository;
        this.pointHistoryRepository = pointHistoryRepository;
        this.userRepository = userRepository;
        this.pointsService = pointsService;
        this.brevoEmailService = brevoEmailService;
        this.templateEngine = templateEngine;
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

            // Thêm discountAmount (số tiền giảm từ điểm) vào embed_data nếu có
            if (order.getDiscountAmount() != null && order.getDiscountAmount() > 0) {
                embedData.put("discountAmount", order.getDiscountAmount());
            }

            // Thêm embedData từ PaymentRequest nếu có (dành cho ATM)
            if (request.getEmbedData() != null && !request.getEmbedData().isEmpty()) {
                try {
                    // Parse embedData từ request (JSON string) và merge vào embedData hiện tại
                    @SuppressWarnings("unchecked")
                    Map<String, Object> additionalEmbedData = objectMapper.readValue(request.getEmbedData(), Map.class);
                    embedData.putAll(additionalEmbedData);
                } catch (JsonProcessingException e) {
                    System.err.println("Failed to parse embedData from request: " + e.getMessage());
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

            System.err.println("ZaloPay request body: " + body);
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
                return "OK";
            }

            // Verify MAC signature với key2
            String dataStr = callback.getData();
            HmacUtils hmacUtils = new HmacUtils(HmacAlgorithms.HMAC_SHA_256, key2);
            String expectedMac = hmacUtils.hmacHex(dataStr);

            if (!expectedMac.equals(callback.getMac())) {
                System.err.println("Invalid MAC signature. Expected: " + expectedMac + ", Got: " + callback.getMac());
                return "OK";
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

            // Lấy Order từ DB
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

            // Lấy discountAmount từ embed_data - Đổi tên biến để tránh conflict
            Integer embedDiscountAmount = null;
            if (callbackData.containsKey("embed_data")) {
                String embedDataStr = (String) callbackData.get("embed_data");
                try {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> embedDataMap = objectMapper.readValue(embedDataStr, Map.class);
                    if (embedDataMap.containsKey("discountAmount")) {
                        embedDiscountAmount = (Integer) embedDataMap.get("discountAmount");
                    }
                } catch (Exception e) {
                    System.err.println("Failed to parse embed_data in callback: " + e.getMessage());
                }
            }

            // Trừ điểm và lưu log nếu embedDiscountAmount > 0
            if (embedDiscountAmount != null && embedDiscountAmount > 0) {
                if (order.getUserId() != null) {
                    pointsService.usePointsOnOrder(order.getUserId(), orderId, embedDiscountAmount,
                            "Dùng điểm thanh toán đơn hàng #" + orderId);
                }
            }

            // Cộng điểm thưởng 2% giá trị đơn hàng nếu thanh toán thành công
            int rewardAmount = 0;
            if (order.getUserId() != null && order.getTotalPrice() != null) {
                rewardAmount = (int) Math.round(order.getTotalPrice().doubleValue() * 0.02);
                if (rewardAmount > 0) {
                    pointsService.addPointsOnOrder(order.getUserId(), orderId, rewardAmount,
                            "Cộng điểm thanh toán đơn hàng #" + orderId);
                }
            }

            // Gửi email thông báo đơn hàng thành công
            try {
                User user = userRepository.findById(order.getUserId()).orElse(null);
                if (user != null && user.getEmail() != null) {
                    String subject = "Đơn hàng của bạn đã thanh toán thành công";
                    Context context = new Context();
                    context.setVariable("fullName", user.getFullName() != null ? user.getFullName() : user.getEmail());
                    context.setVariable("orderId", orderId);

                    // Truyền danh sách sản phẩm với format đúng
                    List<Map<String, Object>> orderItems = new ArrayList<>();
                    List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
                    for (OrderItem item : items) {
                        Map<String, Object> itemMap = new HashMap<>();
                        itemMap.put("foodName", item.getFood() != null ? item.getFood().getName() : "");
                        itemMap.put("quantity", item.getQuantity() != null ? item.getQuantity() : 0);

                        // Format giá tiền đúng cách - kiểm tra null trước
                        long price = item.getPrice() != null ? item.getPrice().longValue() : 0;
                        int quantity = item.getQuantity() != null ? item.getQuantity() : 0;
                        long total = price * quantity;

                        itemMap.put("price", item.getPrice()); // Giữ nguyên cho tính toán
                        itemMap.put("priceFormatted", VnCurrencyFormatter.format(price));
                        itemMap.put("totalFormatted", VnCurrencyFormatter.format(total));

                        orderItems.add(itemMap);
                    }
                    context.setVariable("orderItems", orderItems);

                    // Các giá trị tổng - kiểm tra null và đổi tên biến
                    long orderTotalPrice = order.getTotalPrice() != null ? order.getTotalPrice().longValue() : 0;
                    long orderDiscountAmount = order.getDiscountAmount() != null ? order.getDiscountAmount().longValue() : 0;

                    context.setVariable("totalPriceFormatted", VnCurrencyFormatter.format(orderTotalPrice));
                    context.setVariable("discountAmountFormatted", VnCurrencyFormatter.format(orderDiscountAmount));

                    // Thêm các giá trị không format cho điều kiện check
                    context.setVariable("discountAmount", order.getDiscountAmount());
                    context.setVariable("totalPrice", order.getTotalPrice());
                    context.setVariable("orderStatus", order.getStatus() != null ? order.getStatus().name() : "UNKNOWN");

                    // Thông tin giao hàng - kiểm tra null
                    context.setVariable("receiverName", order.getReceiverName() != null ? order.getReceiverName() : "");
                    context.setVariable("receiverPhone", order.getReceiverPhone() != null ? order.getReceiverPhone() : "");
                    context.setVariable("receiverEmail", order.getReceiverEmail() != null ? order.getReceiverEmail() : "");
                    context.setVariable("deliveryAddress", order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "");
                    context.setVariable("deliveryType", order.getDeliveryType() != null ? order.getDeliveryType() : "");
                    context.setVariable("paymentMethod", order.getPaymentMethod() != null ? order.getPaymentMethod() : "");

                    String htmlContent = templateEngine.process("order_success_email.html", context);
//                    logger.info("Nội dung email gửi cho khách hàng: \n" + htmlContent);
                    brevoEmailService.sendEmail(user.getEmail(), subject, htmlContent);
                }
            } catch (Exception emailEx) {
                // Log lỗi gửi email, không throw exception để không ảnh hưởng callback
                logger.error("Gửi email thông báo đơn hàng thất bại: " + emailEx.getMessage(), emailEx);
            }

            // ZaloPay chỉ gửi callback khi thanh toán THÀNH CÔNG
            updateOrderPaymentStatus(orderId, zpTransId, "PAID");

            return "OK";

        } catch (Exception e) {
            // Log error nhưng vẫn return OK để tránh ZaloPay retry
            logger.error("Error processing callback: {}", e.getMessage(), e);
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
