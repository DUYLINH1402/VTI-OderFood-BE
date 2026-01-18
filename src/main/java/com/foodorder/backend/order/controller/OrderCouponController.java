package com.foodorder.backend.order.controller;

import com.foodorder.backend.coupons.dto.response.CouponApplyResult;
import com.foodorder.backend.order.dto.request.OrderItemRequest;
import com.foodorder.backend.order.entity.OrderItem;
import com.foodorder.backend.order.service.OrderCouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller cho việc áp dụng Coupon trong Order
 * Cung cấp API để validate và preview coupon discount trước khi tạo order
 */
@RestController
@RequestMapping("/api/v1/orders/coupon")
@Slf4j
@Tag(name = "Order Coupons", description = "API áp dụng mã giảm giá cho đơn hàng")
public class OrderCouponController {

    @Autowired
    private OrderCouponService orderCouponService;

    @Operation(summary = "Preview giảm giá coupon", description = "Xem trước số tiền giảm giá khi áp dụng coupon cho đơn hàng.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "400", description = "Coupon không hợp lệ")
    })
    @PostMapping("/preview")
    public ResponseEntity<CouponApplyResult> previewCouponDiscount(
            @RequestBody Map<String, Object> request) {

        String couponCode = (String) request.get("couponCode");
        Long userId = Long.valueOf(request.get("userId").toString());
        Double orderAmount = Double.valueOf(request.get("orderAmount").toString());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> itemsData = (List<Map<String, Object>>) request.get("items");

        // Convert items data to OrderItem entities cho validation
        List<OrderItem> orderItems = itemsData.stream()
                .map(item -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setFoodId(Long.valueOf(item.get("foodId").toString()));
                    orderItem.setQuantity(Integer.valueOf(item.get("quantity").toString()));
                    return orderItem;
                })
                .collect(Collectors.toList());

        log.info("Previewing coupon {} for user {} with amount {}", couponCode, userId, orderAmount);

        CouponApplyResult result = orderCouponService.validateCouponForOrder(
                couponCode, userId, orderAmount, orderItems
        );

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Validate coupon (simple)", description = "Validate coupon cho mobile app - phiên bản đơn giản.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "400", description = "Coupon không hợp lệ")
    })
    @GetMapping("/validate")
    public ResponseEntity<CouponApplyResult> validateCoupon(
            @Parameter(description = "Mã coupon") @RequestParam String code,
            @Parameter(description = "ID người dùng") @RequestParam Long userId,
            @Parameter(description = "Tổng tiền đơn hàng") @RequestParam Double amount,
            @Parameter(description = "Danh sách ID món ăn (cách nhau bởi dấu phẩy)") @RequestParam(required = false) String foodIds) {

        log.info("Validating coupon {} for user {} with amount {}", code, userId, amount);

        // Parse food IDs
        List<OrderItem> orderItems = List.of(); // Empty list nếu không có foodIds
        if (foodIds != null && !foodIds.isEmpty()) {
            orderItems = java.util.Arrays.stream(foodIds.split(","))
                    .map(id -> {
                        OrderItem item = new OrderItem();
                        item.setFoodId(Long.valueOf(id.trim()));
                        item.setQuantity(1); // Default quantity
                        return item;
                    })
                    .collect(Collectors.toList());
        }

        CouponApplyResult result = orderCouponService.validateCouponForOrder(
                code, userId, amount, orderItems
        );

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Xác nhận sử dụng coupon", description = "API cho admin/system xác nhận sử dụng coupon sau khi đơn hàng thành công.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xác nhận thành công"),
            @ApiResponse(responseCode = "400", description = "Lỗi xác nhận")
    })
    @PostMapping("/confirm-usage")
    public ResponseEntity<Map<String, String>> confirmCouponUsage(
            @Parameter(description = "ID đơn hàng") @RequestParam Long orderId,
            @Parameter(description = "Mã coupon") @RequestParam String couponCode,
            @Parameter(description = "ID người dùng") @RequestParam Long userId,
            @Parameter(description = "Số tiền giảm giá") @RequestParam Double discountAmount) {

        log.info("Confirming coupon usage: order={}, coupon={}, user={}, discount={}",
                orderId, couponCode, userId, discountAmount);

        try {
            // Create a simple order object for confirmation
            com.foodorder.backend.order.entity.Order order = new com.foodorder.backend.order.entity.Order();
            order.setId(orderId);
            order.setCouponCode(couponCode);
            order.setUserId(userId);
            order.setCouponDiscountAmount(java.math.BigDecimal.valueOf(discountAmount));

            orderCouponService.confirmCouponUsage(order);

            return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Coupon usage confirmed successfully"
            ));
        } catch (Exception e) {
            log.error("Error confirming coupon usage: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "ERROR",
                "message", "Failed to confirm coupon usage: " + e.getMessage()
            ));
        }
    }

    /**
     * Exception handler
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        log.error("OrderCouponController error: {}", e.getMessage(), e);
        return ResponseEntity.badRequest().body(Map.of(
            "error", "COUPON_OPERATION_FAILED",
            "message", e.getMessage()
        ));
    }
}
