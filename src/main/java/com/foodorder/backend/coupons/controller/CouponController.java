package com.foodorder.backend.coupons.controller;

import com.foodorder.backend.coupons.dto.request.ApplyCouponRequest;
import com.foodorder.backend.coupons.dto.request.CouponRequest;
import com.foodorder.backend.coupons.dto.response.CouponApplyResult;
import com.foodorder.backend.coupons.dto.response.CouponResponse;
import com.foodorder.backend.coupons.entity.CouponStatus;
import com.foodorder.backend.coupons.service.CouponService;
import com.foodorder.backend.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import java.util.Map;

/**
 * REST Controller cho Coupon Management System
 * Cung cấp đầy đủ API cho việc quản lý và sử dụng coupon
 */
@RestController
@RequestMapping("/api/coupons")
@Validated
public class CouponController {

    @Autowired
    private CouponService couponService;

    // === QUẢN LÝ COUPON CƠ BẢN (ADMIN) ===

    /**
     * Tạo mới coupon
     * POST /api/coupons
     */
    @PostMapping
    public ResponseEntity<CouponResponse> createCoupon(@RequestBody @Valid CouponRequest request) {
        CouponResponse response = couponService.createCoupon(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật coupon
     * PUT /api/coupons/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<CouponResponse> updateCoupon(
            @PathVariable Long id,
            @RequestBody @Valid CouponRequest request) {
        CouponResponse response = couponService.updateCoupon(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa coupon (soft delete)
     * DELETE /api/coupons/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCoupon(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lấy thông tin coupon theo ID
     * GET /api/coupons/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CouponResponse> getCouponById(@PathVariable Long id) {
        return couponService.getCouponById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lấy thông tin coupon theo code
     * GET /api/coupons/code/{code}
     */
    @GetMapping("/code/{code}")
    public ResponseEntity<CouponResponse> getCouponByCode(@PathVariable String code) {
        return couponService.getCouponByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lấy danh sách tất cả coupon với phân trang
     * GET /api/coupons?page=0&size=10&sort=createdAt,desc
     */
    @GetMapping
    public ResponseEntity<Page<CouponResponse>> getAllCoupons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Pageable pageable = PageRequest.of(page, size,
            sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending());

        Page<CouponResponse> response = couponService.getAllCoupons(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách coupon theo trạng thái
     * GET /api/coupons/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<CouponResponse>> getCouponsByStatus(@PathVariable CouponStatus status) {
        List<CouponResponse> response = couponService.getCouponsByStatus(status);
        return ResponseEntity.ok(response);
    }

    // === API CHO USER (PUBLIC) ===

    /**
     * Lấy danh sách coupon công khai đang hoạt động
     * GET /api/coupons/public/active
     */
    @GetMapping("/public/active")
    public ResponseEntity<List<CouponResponse>> getActivePublicCoupons() {
        List<CouponResponse> response = couponService.getActivePublicCoupons();
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách coupon available cho user hiện tại (từ token)
     * GET /api/coupons/available
     */
    @GetMapping("/available")
    public ResponseEntity<List<CouponResponse>> getAvailableCouponsForCurrentUser() {
        // Lấy thông tin user từ token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getId();

        List<CouponResponse> response = couponService.getAvailableCouponsForUser(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Validate coupon cho đơn hàng (không áp dụng thực tế)
     * POST /api/coupons/validate
     */
    @PostMapping("/validate")
    public ResponseEntity<CouponApplyResult> validateCoupon(@RequestBody @Valid ApplyCouponRequest request, Authentication authentication) {
        // Lấy userId từ SecurityContext nếu đã đăng nhập
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            Long userId = ((CustomUserDetails) authentication.getPrincipal()).getId();
            request.setUserId(userId);
        }
        CouponApplyResult result = couponService.validateCouponForOrder(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Áp dụng coupon vào đơn hàng (tính toán giảm giá)
     * POST /api/coupons/apply
     */
    @PostMapping("/apply")
    public ResponseEntity<CouponApplyResult> applyCoupon(@RequestBody @Valid ApplyCouponRequest request) {
        CouponApplyResult result = couponService.applyCouponToOrder(request);
        return ResponseEntity.ok(result);
    }

    // === QUẢN LÝ TRẠNG THÁI COUPON (ADMIN) ===

    /**
     * Kích hoạt coupon
     * PUT /api/coupons/{id}/activate
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activateCoupon(@PathVariable Long id) {
        couponService.activateCoupon(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Vô hiệu hóa coupon
     * PUT /api/coupons/{id}/deactivate
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateCoupon(@PathVariable Long id) {
        couponService.deactivateCoupon(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Cập nhật trạng thái coupon hết hạn (manual trigger)
     * PUT /api/coupons/update-expired
     */
    @PutMapping("/update-expired")
    public ResponseEntity<Void> updateExpiredCoupons() {
        couponService.updateExpiredCoupons();
        return ResponseEntity.ok().build();
    }

    /**
     * Cập nhật trạng thái coupon hết lượt sử dụng (manual trigger)
     * PUT /api/coupons/update-used-out
     */
    @PutMapping("/update-used-out")
    public ResponseEntity<Void> updateUsedOutCoupons() {
        couponService.updateUsedOutCoupons();
        return ResponseEntity.ok().build();
    }

    // === BÁO CÁO VÀ THỐNG KÊ (ADMIN) ===

    /**
     * Thống kê coupon theo trạng thái
     * GET /api/coupons/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<CouponStatus, Long>> getCouponStatistics() {
        Map<CouponStatus, Long> stats = couponService.getCouponStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Top coupon được sử dụng nhiều nhất
     * GET /api/coupons/most-used?limit=10
     */
    @GetMapping("/most-used")
    public ResponseEntity<List<CouponResponse>> getMostUsedCoupons(
            @RequestParam(defaultValue = "10") @Min(1) int limit) {
        List<CouponResponse> response = couponService.getMostUsedCoupons(limit);
        return ResponseEntity.ok(response);
    }

    /**
     * Danh sách coupon sắp hết hạn
     * GET /api/coupons/expiring-soon?days=7
     */
    @GetMapping("/expiring-soon")
    public ResponseEntity<List<CouponResponse>> getExpiringSoonCoupons(
            @RequestParam(defaultValue = "7") @Min(1) int days) {
        List<CouponResponse> response = couponService.getExpiringSoonCoupons(days);
        return ResponseEntity.ok(response);
    }

    // === BUSINESS OPERATIONS (ADMIN) ===

    /**
     * Tạo coupon sinh nhật cho user
     * POST /api/coupons/birthday/{userId}
     */
    @PostMapping("/birthday/{userId}")
    public ResponseEntity<Void> createBirthdayCoupon(@PathVariable Long userId) {
        couponService.createBirthdayCouponForUser(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Tạo coupon đơn hàng đầu tiên cho user mới
     * POST /api/coupons/welcome/{userId}
     */
    @PostMapping("/welcome/{userId}")
    public ResponseEntity<Void> createWelcomeCoupon(@PathVariable Long userId) {
        couponService.createFirstOrderCouponForUser(userId);
        return ResponseEntity.ok().build();
    }

    /**
     * Gửi coupon riêng tư cho danh sách user
     * POST /api/coupons/{couponId}/send-to-users
     */
    @PostMapping("/{couponId}/send-to-users")
    public ResponseEntity<Void> sendCouponToUsers(
            @PathVariable Long couponId,
            @RequestBody List<Long> userIds) {
        couponService.sendPrivateCouponToUsers(couponId, userIds);
        return ResponseEntity.ok().build();
    }

    // === INTERNAL API (CHO ORDER SERVICE) ===

    /**
     * Xác nhận sử dụng coupon (được gọi từ Order Service)
     * POST /api/coupons/confirm-usage
     */
    @PostMapping("/confirm-usage")
    public ResponseEntity<Void> confirmCouponUsage(
            @RequestParam String couponCode,
            @RequestParam Long userId,
            @RequestParam Long orderId,
            @RequestParam Double discountAmount) {
        couponService.confirmCouponUsage(couponCode, userId, orderId, discountAmount);
        return ResponseEntity.ok().build();
    }

    /**
     * Hủy sử dụng coupon (khi đơn hàng bị hủy)
     * DELETE /api/coupons/usage/{usageId}
     */
    @DeleteMapping("/usage/{usageId}")
    public ResponseEntity<Void> cancelCouponUsage(@PathVariable Long usageId) {
        couponService.cancelCouponUsage(usageId);
        return ResponseEntity.ok().build();
    }

    // === EXCEPTION HANDLING ===

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid argument: " + e.getMessage()));
    }
}
