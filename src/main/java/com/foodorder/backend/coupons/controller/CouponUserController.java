package com.foodorder.backend.coupons.controller;

import com.foodorder.backend.coupons.dto.request.ApplyCouponRequest;
import com.foodorder.backend.coupons.dto.response.CouponApplyResult;
import com.foodorder.backend.coupons.dto.response.CouponResponse;
import com.foodorder.backend.coupons.service.CouponService;
import com.foodorder.backend.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller cho User sử dụng Coupon
 * Cung cấp API để user xem, validate và áp dụng mã giảm giá
 */
@RestController
@RequestMapping("/api/coupons")
@Validated
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Coupons", description = "API mã giảm giá dành cho người dùng")
public class CouponUserController {

    private final CouponService couponService;

    // === API CÔNG KHAI ===

    /**
     * Lấy danh sách coupon công khai đang hoạt động
     * GET /api/coupons/public/active
     */
    @Operation(summary = "Danh sách coupon công khai", description = "Lấy danh sách mã giảm giá công khai đang hoạt động.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/public/active")
    public ResponseEntity<List<CouponResponse>> getActivePublicCoupons() {
        List<CouponResponse> response = couponService.getActivePublicCoupons();
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy thông tin chi tiết coupon theo mã code
     * GET /api/coupons/code/{code}
     */
    @Operation(summary = "Chi tiết coupon (Code)", description = "Lấy thông tin chi tiết mã giảm giá theo mã code.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy coupon")
    })
    @GetMapping("/code/{code}")
    public ResponseEntity<CouponResponse> getCouponByCode(
            @Parameter(description = "Mã code của coupon") @PathVariable String code) {
        return couponService.getCouponByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // === API YÊU CẦU ĐĂNG NHẬP ===

    /**
     * Lấy danh sách coupon khả dụng cho user hiện tại
     * GET /api/coupons/available
     */
    @Operation(summary = "Coupon khả dụng cho user", description = "Lấy danh sách mã giảm giá mà user hiện tại có thể sử dụng.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
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
     * Kiểm tra tính hợp lệ của coupon cho đơn hàng (không áp dụng thực tế)
     * POST /api/coupons/validate
     */
    @Operation(summary = "Kiểm tra coupon", description = "Kiểm tra tính hợp lệ của mã giảm giá cho đơn hàng (không áp dụng thực tế).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công - Trả về kết quả kiểm tra")
    })
    @PostMapping("/validate")
    public ResponseEntity<CouponApplyResult> validateCoupon(
            @RequestBody @Valid ApplyCouponRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        // Lấy userId từ SecurityContext nếu đã đăng nhập
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            try {
                CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
                if (userDetails != null) {
                    Long userId = userDetails.getId();
                    request.setUserId(userId);
                }
            } catch (Exception e) {
                log.error("Error getting userId from CustomUserDetails: {}", e.getMessage());
                // Continue without userId
            }
        }
        CouponApplyResult result = couponService.validateCouponForOrder(request);
        return ResponseEntity.ok(result);
    }

    /**
     * Áp dụng mã giảm giá vào đơn hàng và tính toán số tiền giảm
     * POST /api/coupons/apply
     */
    @Operation(summary = "Áp dụng coupon", description = "Áp dụng mã giảm giá vào đơn hàng và tính toán số tiền giảm.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công - Trả về kết quả giảm giá"),
            @ApiResponse(responseCode = "400", description = "Coupon không hợp lệ")
    })
    @PostMapping("/apply")
    public ResponseEntity<CouponApplyResult> applyCoupon(@RequestBody @Valid ApplyCouponRequest request) {
        CouponApplyResult result = couponService.applyCouponToOrder(request);
        return ResponseEntity.ok(result);
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

