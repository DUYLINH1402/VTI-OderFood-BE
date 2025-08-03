package com.foodorder.backend.order.service;

import com.foodorder.backend.coupons.dto.request.ApplyCouponRequest;
import com.foodorder.backend.coupons.dto.response.CouponApplyResult;
import com.foodorder.backend.coupons.service.CouponIntegrationService;
import com.foodorder.backend.order.entity.Order;
import com.foodorder.backend.order.entity.OrderItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service tích hợp Coupon với Order System
 * Xử lý logic áp dụng coupon trong quá trình đặt hàng
 */
@Service
@Slf4j
public class OrderCouponService {

    @Autowired
    private CouponIntegrationService couponIntegrationService;

    /**
     * Áp dụng coupon cho đơn hàng trước khi tạo order
     * @param couponCode Mã coupon
     * @param userId ID người dùng
     * @param orderAmount Tổng tiền đơn hàng
     * @param orderItems Danh sách item trong đơn hàng
     * @return Kết quả áp dụng coupon
     */
    public CouponApplyResult applyCouponToOrder(String couponCode, Long userId,
                                                Double orderAmount, List<OrderItem> orderItems) {
        log.info("Applying coupon {} for user {} with order amount {}", couponCode, userId, orderAmount);

        // Lấy danh sách food IDs từ order items
        List<Long> foodIds = orderItems.stream()
                .map(OrderItem::getFoodId)
                .collect(Collectors.toList());

        // Gọi coupon service để áp dụng
        CouponApplyResult result = couponIntegrationService.applyCouponToOrder(
                couponCode, userId, orderAmount, foodIds
        );

        if (result.getSuccess()) {
            log.info("Successfully applied coupon {} for user {}, discount: {}",
                    couponCode, userId, result.getDiscountAmount());
        } else {
            log.warn("Failed to apply coupon {} for user {}: {}",
                    couponCode, userId, result.getMessage());
        }

        return result;
    }

    /**
     * Tính toán tổng tiền sau khi áp dụng coupon
     * @param originalAmount Tổng tiền gốc
     * @param couponResult Kết quả áp dụng coupon
     * @return Tổng tiền sau khi giảm
     */
    public BigDecimal calculateFinalAmount(BigDecimal originalAmount, CouponApplyResult couponResult) {
        if (!couponResult.getSuccess() || couponResult.getDiscountAmount() == null) {
            return originalAmount;
        }

        BigDecimal discountAmount = BigDecimal.valueOf(couponResult.getDiscountAmount());
        BigDecimal finalAmount = originalAmount.subtract(discountAmount);

        // Đảm bảo không âm
        return finalAmount.max(BigDecimal.ZERO);
    }

    /**
     * Cập nhật thông tin coupon vào order entity
     * @param order Order entity cần cập nhật
     * @param couponResult Kết quả áp dụng coupon
     */
    public void updateOrderWithCouponInfo(Order order, CouponApplyResult couponResult) {
        if (couponResult.getSuccess()) {
            order.setCouponCode(couponResult.getCouponCode());
            order.setCouponDiscountAmount(BigDecimal.valueOf(couponResult.getDiscountAmount()));
            order.setOriginalAmount(BigDecimal.valueOf(couponResult.getOriginalAmount()));

            // Cập nhật total price = original amount - coupon discount
            BigDecimal finalAmount = calculateFinalAmount(
                    BigDecimal.valueOf(couponResult.getOriginalAmount()),
                    couponResult
            );
            order.setTotalPrice(finalAmount);
        }
    }

    /**
     * Xác nhận sử dụng coupon sau khi order thành công
     * @param order Order đã được tạo thành công
     */
    public void confirmCouponUsage(Order order) {
        if (order.getCouponCode() != null && order.getCouponDiscountAmount() != null) {
            log.info("Confirming coupon usage for order {}", order.getId());

            try {
                couponIntegrationService.confirmCouponUsageForOrder(
                        order.getCouponCode(),
                        order.getUserId(),
                        order.getId(),
                        order.getCouponDiscountAmount().doubleValue()
                );
                log.info("Successfully confirmed coupon usage for order {}", order.getId());
            } catch (Exception e) {
                log.error("Failed to confirm coupon usage for order {}: {}",
                        order.getId(), e.getMessage(), e);
                // Không throw exception để không ảnh hưởng đến order flow
            }
        }
    }

    /**
     * Hủy sử dụng coupon khi order bị hủy
     * @param order Order bị hủy
     */
    public void cancelCouponUsage(Order order) {
        if (order.getCouponCode() != null) {
            log.info("Cancelling coupon usage for cancelled order {}", order.getId());

            try {
                couponIntegrationService.cancelCouponUsageForOrder(order.getId());
                log.info("Successfully cancelled coupon usage for order {}", order.getId());
            } catch (Exception e) {
                log.error("Failed to cancel coupon usage for order {}: {}",
                        order.getId(), e.getMessage(), e);
                // Log lỗi nhưng không throw để không ảnh hưởng cancel flow
            }
        }
    }

    /**
     * Validate coupon trước khi tạo order (optional - để UI hiển thị preview)
     * @param couponCode Mã coupon
     * @param userId ID người dùng
     * @param orderAmount Tổng tiền đơn hàng
     * @param orderItems Danh sách item
     * @return Kết quả validation
     */
    public CouponApplyResult validateCouponForOrder(String couponCode, Long userId,
                                                    Double orderAmount, List<OrderItem> orderItems) {
        log.info("Validating coupon {} for user {} with amount {}", couponCode, userId, orderAmount);

        List<Long> foodIds = orderItems.stream()
                .map(OrderItem::getFoodId)
                .collect(Collectors.toList());

        return couponIntegrationService.applyCouponToOrder(couponCode, userId, orderAmount, foodIds);
    }
}
