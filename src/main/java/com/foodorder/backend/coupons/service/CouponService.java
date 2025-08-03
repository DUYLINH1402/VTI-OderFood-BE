package com.foodorder.backend.coupons.service;

import com.foodorder.backend.coupons.dto.request.ApplyCouponRequest;
import com.foodorder.backend.coupons.dto.request.CouponRequest;
import com.foodorder.backend.coupons.dto.response.CouponApplyResult;
import com.foodorder.backend.coupons.dto.response.CouponResponse;
import com.foodorder.backend.coupons.entity.Coupon;
import com.foodorder.backend.coupons.entity.CouponStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface cho nghiệp vụ Coupon
 * Định nghĩa các chức năng chính: tạo, cập nhật, xóa, lấy danh sách, kiểm tra điều kiện áp dụng coupon
 */
public interface CouponService {

    // === QUẢN LÝ COUPON CƠ BẢN ===
    /**
     * Tạo mới coupon
     */
    CouponResponse createCoupon(CouponRequest request);

    /**
     * Cập nhật coupon
     */
    CouponResponse updateCoupon(Long id, CouponRequest request);

    /**
     * Xóa coupon (soft delete - chuyển sang trạng thái INACTIVE)
     */
    void deleteCoupon(Long id);

    /**
     * Lấy thông tin coupon theo id
     */
    Optional<CouponResponse> getCouponById(Long id);

    /**
     * Lấy thông tin coupon theo mã code
     */
    Optional<CouponResponse> getCouponByCode(String code);

    /**
     * Lấy danh sách coupon theo trạng thái
     */
    List<CouponResponse> getCouponsByStatus(CouponStatus status);

    /**
     * Lấy danh sách coupon có phân trang
     */
    Page<CouponResponse> getAllCoupons(Pageable pageable);

    // === LOGIC NGHIỆP VỤ COUPON ===
    /**
     * Lấy danh sách coupon công khai đang hoạt động
     */
    List<CouponResponse> getActivePublicCoupons();

    /**
     * Lấy danh sách coupon mà user có thể sử dụng
     */
    List<CouponResponse> getAvailableCouponsForUser(Long userId);

    /**
     * Kiểm tra coupon có thể áp dụng cho đơn hàng không
     */
    CouponApplyResult validateCouponForOrder(ApplyCouponRequest request);

    /**
     * Áp dụng coupon cho đơn hàng (tính toán giảm giá)
     */
    CouponApplyResult applyCouponToOrder(ApplyCouponRequest request);

    /**
     * Xác nhận sử dụng coupon (sau khi đơn hàng thành công)
     */
    void confirmCouponUsage(String couponCode, Long userId, Long orderId, Double discountAmount);

    /**
     * Hủy sử dụng coupon (khi đơn hàng bị hủy)
     */
    void cancelCouponUsage(Long couponUsageId);

    // === QUẢN LÝ TRẠNG THÁI COUPON ===
    /**
     * Kích hoạt coupon
     */
    void activateCoupon(Long couponId);

    /**
     * Vô hiệu hóa coupon
     */
    void deactivateCoupon(Long couponId);

    /**
     * Cập nhật trạng thái coupon hết hạn (chạy định kỳ)
     */
    void updateExpiredCoupons();

    /**
     * Cập nhật trạng thái coupon hết lượt sử dụng
     */
    void updateUsedOutCoupons();

    // === BÁO CÁO VÀ THỐNG KÊ ===
    /**
     * Thống kê số lượng coupon theo trạng thái
     */
    java.util.Map<CouponStatus, Long> getCouponStatistics();

    /**
     * Lấy top coupon được sử dụng nhiều nhất
     */
    List<CouponResponse> getMostUsedCoupons(int limit);

    /**
     * Lấy danh sách coupon sắp hết hạn (trong vòng X ngày)
     */
    List<CouponResponse> getExpiringSoonCoupons(int daysAhead);

    // === HỖ TRỢ BUSINESS ===
    /**
     * Tự động tạo coupon sinh nhật cho user
     */
    void createBirthdayCouponForUser(Long userId);

    /**
     * Tự động tạo coupon đơn hàng đầu tiên cho user mới
     */
    void createFirstOrderCouponForUser(Long userId);

    /**
     * Gửi coupon riêng tư cho danh sách user
     */
    void sendPrivateCouponToUsers(Long couponId, List<Long> userIds);
}
