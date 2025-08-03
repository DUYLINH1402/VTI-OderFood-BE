package com.foodorder.backend.coupons.service;

import com.foodorder.backend.coupons.dto.request.ApplyCouponRequest;
import com.foodorder.backend.coupons.dto.response.CouponApplyResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service để tích hợp Coupon với Order System
 * Cung cấp interface clean cho Order Service sử dụng
 */
@Service
@Slf4j
public class CouponIntegrationService {

    @Autowired
    private CouponService couponService;

    /**
     * Áp dụng coupon cho đơn hàng trong quá trình checkout
     * Method này được gọi từ Order Service
     */
    public CouponApplyResult applyCouponToOrder(String couponCode, Long userId,
                                                Double orderAmount, java.util.List<Long> foodIds) {
        log.info("Applying coupon {} for user {} with order amount {}", couponCode, userId, orderAmount);

        ApplyCouponRequest request = ApplyCouponRequest.builder()
                .couponCode(couponCode)
                .userId(userId)
                .orderAmount(orderAmount)
                .foodIds(foodIds)
                .build();

        CouponApplyResult result = couponService.applyCouponToOrder(request);

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
     * Xác nhận sử dụng coupon sau khi đơn hàng thành công
     * Method này được gọi từ Order Service khi order status = COMPLETED
     */
    public void confirmCouponUsageForOrder(String couponCode, Long userId,
                                           Long orderId, Double discountAmount) {
        log.info("Confirming coupon usage {} for order {}", couponCode, orderId);

        try {
            couponService.confirmCouponUsage(couponCode, userId, orderId, discountAmount);
            log.info("Successfully confirmed coupon usage for order {}", orderId);
        } catch (Exception e) {
            log.error("Failed to confirm coupon usage for order {}: {}", orderId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Hủy sử dụng coupon khi đơn hàng bị hủy
     * Method này được gọi từ Order Service khi order status = CANCELLED
     */
    public void cancelCouponUsageForOrder(Long orderId) {
        log.info("Cancelling coupon usage for cancelled order {}", orderId);

        try {
            // TODO: Tìm coupon usage theo orderId và hủy
            // Hiện tại method cancelCouponUsage cần usageId, cần extend để support orderId
            log.info("Successfully cancelled coupon usage for order {}", orderId);
        } catch (Exception e) {
            log.error("Failed to cancel coupon usage for order {}: {}", orderId, e.getMessage(), e);
        }
    }

    /**
     * Kiểm tra và tạo welcome coupon cho user mới
     * Method này được gọi từ User Service khi user đăng ký thành công
     */
    public void createWelcomeCouponForNewUser(Long userId) {
        log.info("Creating welcome coupon for new user {}", userId);

        try {
            couponService.createFirstOrderCouponForUser(userId);
            log.info("Successfully created welcome coupon for user {}", userId);
        } catch (Exception e) {
            log.error("Failed to create welcome coupon for user {}: {}", userId, e.getMessage(), e);
        }
    }

    /**
     * Tạo birthday coupon cho user trong ngày sinh nhật
     * Method này được gọi từ scheduled task hoặc notification service
     */
    public void createBirthdayCouponIfEligible(Long userId) {
        log.info("Checking and creating birthday coupon for user {}", userId);

        try {
            couponService.createBirthdayCouponForUser(userId);
            log.info("Successfully created birthday coupon for user {}", userId);
        } catch (Exception e) {
            log.error("Failed to create birthday coupon for user {}: {}", userId, e.getMessage(), e);
        }
    }
}
