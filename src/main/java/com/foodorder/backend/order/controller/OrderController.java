package com.foodorder.backend.order.controller;

import com.foodorder.backend.order.dto.request.OrderRequest;
import com.foodorder.backend.order.dto.request.UpdateOrderStatusRequest;
import com.foodorder.backend.order.dto.request.CancelOrderRequest;
import com.foodorder.backend.order.dto.response.OrderResponse;
import com.foodorder.backend.order.dto.response.OrderStatisticsResponse;
import com.foodorder.backend.order.dto.response.PageResponse;
import com.foodorder.backend.order.dto.response.ApiResponse;
import com.foodorder.backend.order.service.OrderService;
import com.foodorder.backend.order.config.PaymentConfig;
import com.foodorder.backend.order.exception.UnauthorizedException;
import com.foodorder.backend.payments.dto.request.PaymentRequest;
import com.foodorder.backend.payments.dto.response.PaymentResponse;
import com.foodorder.backend.payments.controller.PaymentController;
import com.foodorder.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
    private final PaymentController paymentController;

    @PostMapping
    public ResponseEntity<PaymentResponse> createOrderAndPay(@RequestBody OrderRequest orderRequest) {

        // Bước 1: Tạo đơn hàng trước
        OrderResponse orderResponse = orderService.createOrder(orderRequest);

        // Bước 2: Lấy payment config dựa trên payment method
        PaymentConfig paymentConfig = PaymentConfig.getPaymentConfig(orderRequest.getPaymentMethod());

        // Bước 3: Tạo payment request với bankCode và embedData đúng
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId(orderResponse.getId());
        paymentRequest.setPaymentMethod(orderRequest.getPaymentMethod().name());
        paymentRequest.setBankCode(paymentConfig.getBankCode());

        // Thêm embedData nếu có (dành cho ATM)
        if (!paymentConfig.getEmbedData().isEmpty()) {
            paymentRequest.setEmbedData(paymentConfig.getEmbedData());
        }

        // Bước 4: Gọi PaymentController để tạo link thanh toán
        PaymentResponse paymentResponse = paymentController.createPayment(paymentRequest);

        return ResponseEntity.ok(paymentResponse);
    }

    // API lấy danh sách đơn hàng của user hiện tại
    @GetMapping
    public ResponseEntity<PageResponse<OrderResponse>> getOrders(
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Long userId, // Thêm param này để test
            HttpServletRequest request) {

        // Lấy userId từ token hoặc từ param (để test)
        Long actualUserId = userId != null ? userId : getUserIdFromToken(request);

        PageRequest pageRequest = PageRequest.of(page, size,
                Sort.by(Sort.Direction.fromString(sortDir), sortBy));

        PageResponse<OrderResponse> orders = orderService.getOrdersByUser(actualUserId, status, pageRequest);

        return ResponseEntity.ok(orders);
    }

    // API lấy chi tiết đơn hàng theo mã đơn hàng
    @GetMapping("/{orderCode}")
    public ResponseEntity<OrderResponse> getOrderDetail(
            @PathVariable String orderCode,
            HttpServletRequest request) {

        Long userId = getUserIdFromToken(request);
        OrderResponse order = orderService.getOrderDetail(orderCode, userId);

        return ResponseEntity.ok(order);
    }

    // API cập nhật trạng thái đơn hàng
    @PutMapping("/{orderCode}/status")
    public ResponseEntity<ApiResponse> updateOrderStatus(
            @PathVariable String orderCode,
            @RequestBody UpdateOrderStatusRequest request,
            HttpServletRequest httpRequest) {

        Long userId = getUserIdFromToken(httpRequest);
        orderService.updateOrderStatus(orderCode, userId, request);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công"));
    }

    // API hủy đơn hàng
    @PutMapping("/{orderCode}/cancel")
    public ResponseEntity<ApiResponse> cancelOrder(
            @PathVariable String orderCode,
            @RequestBody CancelOrderRequest request,
            HttpServletRequest httpRequest) {

        log.info("Cancelling order: {} with reason: {}", orderCode, request.getCancelReason());

        Long userId = getUserIdFromToken(httpRequest);
        orderService.cancelOrder(orderCode, userId, request.getCancelReason());

        return ResponseEntity.ok(ApiResponse.success("Hủy đơn hàng thành công"));
    }

    // API lấy thống kê đơn hàng
    @GetMapping("/statistics")
    public ResponseEntity<OrderStatisticsResponse> getOrderStatistics(
            HttpServletRequest request) {

        log.info("Getting order statistics");

        Long userId = getUserIdFromToken(request);
        OrderStatisticsResponse stats = orderService.getOrderStatistics(userId);

        return ResponseEntity.ok(stats);
    }

    // Helper method để lấy userId từ Spring Security Context
    private Long getUserIdFromToken(HttpServletRequest request) {
        try {
            // Lấy authentication từ SecurityContextHolder
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();

                if (principal instanceof CustomUserDetails) {
                    CustomUserDetails userDetails = (CustomUserDetails) principal;
                    Long userId = userDetails.getId();
                    return userId;
                } else {
                    log.warn("Principal is not CustomUserDetails, type: {}",
                            principal != null ? principal.getClass().getSimpleName() : "null");
                }
            } else {
                log.warn("Authentication is null or not authenticated");
            }

            // Fallback: Nếu không có authentication context, return userId default cho
            // development/testing
            log.warn("No valid authentication context found, using default userId for testing");
            return 1L; // TODO: Remove this in production

        } catch (Exception e) {
            log.error("Error extracting userId from security context: {}", e.getMessage(), e);
            // Thay vì throw exception, return default userId cho testing
            log.warn("Using fallback userId due to error");
            return 1L;
        }
    }
}
