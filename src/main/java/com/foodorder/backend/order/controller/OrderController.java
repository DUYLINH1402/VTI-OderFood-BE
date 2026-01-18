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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Controller xử lý các nghiệp vụ liên quan đến đơn hàng của người dùng
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders", description = "API quản lý đơn hàng của người dùng")
public class OrderController {

    private final OrderService orderService;
    private final PaymentController paymentController;

    @Operation(summary = "Tạo đơn hàng và thanh toán",
            description = "Tạo đơn hàng mới và khởi tạo link thanh toán dựa trên phương thức được chọn.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tạo đơn hàng thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy món ăn")
    })
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

    @Operation(summary = "Lấy danh sách đơn hàng", description = "Lấy danh sách đơn hàng của người dùng hiện tại với phân trang và lọc theo trạng thái.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping
    public ResponseEntity<PageResponse<OrderResponse>> getOrders(
            @Parameter(description = "Trạng thái đơn hàng (all, pending, confirmed, ...)") @RequestParam(defaultValue = "all") String status,
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "ID người dùng (dùng để test)") @RequestParam(required = false) Long userId,
            @Parameter(hidden = true) HttpServletRequest request) {

        // Lấy userId từ token hoặc từ param (để test)
        Long actualUserId = userId != null ? userId : getUserIdFromToken(request);

        PageRequest pageRequest = PageRequest.of(page, size,
                Sort.by(Sort.Direction.fromString(sortDir), sortBy));

        PageResponse<OrderResponse> orders = orderService.getOrdersByUser(actualUserId, status, pageRequest);

        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Chi tiết đơn hàng", description = "Lấy thông tin chi tiết của một đơn hàng theo mã đơn.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy đơn hàng")
    })
    @GetMapping("/{orderCode}")
    public ResponseEntity<OrderResponse> getOrderDetail(
            @Parameter(description = "Mã đơn hàng") @PathVariable String orderCode,
            @Parameter(hidden = true) HttpServletRequest request) {

        Long userId = getUserIdFromToken(request);
        OrderResponse order = orderService.getOrderDetail(orderCode, userId);

        return ResponseEntity.ok(order);
    }

    @Operation(summary = "Cập nhật trạng thái đơn hàng", description = "Cập nhật trạng thái của một đơn hàng.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy đơn hàng"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Trạng thái không hợp lệ")
    })
    @PutMapping("/{orderCode}/status")
    public ResponseEntity<ApiResponse> updateOrderStatus(
            @Parameter(description = "Mã đơn hàng") @PathVariable String orderCode,
            @RequestBody UpdateOrderStatusRequest request,
            @Parameter(hidden = true) HttpServletRequest httpRequest) {

        Long userId = getUserIdFromToken(httpRequest);
        orderService.updateOrderStatus(orderCode, userId, request);

        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạng thái thành công"));
    }

    @Operation(summary = "Hủy đơn hàng", description = "Hủy một đơn hàng với lý do cụ thể.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Hủy thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Không tìm thấy đơn hàng"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Không thể hủy đơn hàng")
    })
    @PutMapping("/{orderCode}/cancel")
    public ResponseEntity<ApiResponse> cancelOrder(
            @Parameter(description = "Mã đơn hàng") @PathVariable String orderCode,
            @RequestBody CancelOrderRequest request,
            @Parameter(hidden = true) HttpServletRequest httpRequest) {

        log.info("Cancelling order: {} with reason: {}", orderCode, request.getCancelReason());

        Long userId = getUserIdFromToken(httpRequest);
        orderService.cancelOrder(orderCode, userId, request.getCancelReason());

        return ResponseEntity.ok(ApiResponse.success("Hủy đơn hàng thành công"));
    }

    @Operation(summary = "Thống kê đơn hàng", description = "Lấy thống kê đơn hàng của người dùng hiện tại.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Thành công"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping("/statistics")
    public ResponseEntity<OrderStatisticsResponse> getOrderStatistics(
            @Parameter(hidden = true) HttpServletRequest request) {

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
