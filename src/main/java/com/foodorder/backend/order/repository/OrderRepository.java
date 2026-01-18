package com.foodorder.backend.order.repository;

import com.foodorder.backend.order.entity.Order;
import com.foodorder.backend.order.entity.OrderStatus;
import com.foodorder.backend.order.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    // Thêm method để fetch Order với Items
    @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.items WHERE o.id = :id")
    Order findByIdWithItems(@Param("id") Long id);

    // Lấy đơn hàng theo userId với phân trang
    Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, PageRequest pageRequest);

    // Lấy đơn hàng theo userId và status với phân trang
    Page<Order> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, OrderStatus status, PageRequest pageRequest);

    // Lấy đơn hàng theo id và userId
    Optional<Order> findByIdAndUserId(Long id, Long userId);

    // Đếm số đơn hàng theo userId
    long countByUserId(Long userId);

    // Đếm số đơn hàng theo userId và status
    long countByUserIdAndStatus(Long userId, OrderStatus status);

    // Tính tổng tiền đã chi tiêu của user
    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.userId = :userId AND o.status = 'COMPLETED'")
    BigDecimal getTotalSpentByUserId(@Param("userId") Long userId);

    // === MANAGEMENT METHODS ===

    // Tìm đơn hàng theo orderCode
    Optional<Order> findByOrderCode(String orderCode);

    // Đếm đơn hàng theo khoảng thời gian
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate")
    long countOrdersByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Đếm đơn hàng theo status và khoảng thời gian
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    long countOrdersByStatusAndDateRange(@Param("status") OrderStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Tính tổng doanh thu theo khoảng thời gian
    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.status = 'COMPLETED' AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    BigDecimal getTotalRevenueByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Lấy danh sách đơn hàng theo khoảng thời gian (cần thiết cho thống kê)
    @Query("SELECT o FROM Order o WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate")
    java.util.List<Order> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Lấy danh sách đơn hàng gần đây (cho dashboard activities)
     */
    @Query("SELECT o FROM Order o ORDER BY o.createdAt DESC")
    java.util.List<Order> findRecentOrders(org.springframework.data.domain.Pageable pageable);

    /**
     * Đếm số đơn hàng theo status
     */
    long countByStatus(OrderStatus status);

    /**
     * Đếm số đơn hàng theo nhiều status
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status IN :statuses")
    long countByStatusIn(@Param("statuses") java.util.List<OrderStatus> statuses);

    /**
     * Tính tổng doanh thu theo status và khoảng thời gian
     */
    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.status = :status AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    BigDecimal getTotalRevenueByStatusAndDateRange(@Param("status") OrderStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Lấy đơn hàng theo status và khoảng thời gian, sắp xếp theo thời gian mới nhất
     */
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt >= :startDate AND o.createdAt <= :endDate ORDER BY o.createdAt DESC")
    java.util.List<Order> findByStatusAndDateRange(@Param("status") OrderStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // ============ ADMIN DASHBOARD STATISTICS ============

    /**
     * Tính doanh thu thực (đơn COMPLETED và đã thanh toán)
     */
    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.status = 'COMPLETED' AND o.paymentStatus = 'PAID'")
    BigDecimal getActualRevenue();

    /**
     * Tính doanh thu thực theo khoảng thời gian
     */
    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.status = 'COMPLETED' AND o.paymentStatus = 'PAID' AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    BigDecimal getActualRevenueByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Tính tổng doanh thu tất cả đơn (kể cả chưa thanh toán)
     */
    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.status NOT IN ('CANCELLED')")
    BigDecimal getTotalRevenueAll();

    /**
     * Đếm đơn hàng theo status và khoảng thời gian (cho thống kê hôm nay, tuần này, tháng này)
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startDate AND o.createdAt <= :endDate")
    Long countOrdersInDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Đếm đơn hủy theo khoảng thời gian
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'CANCELLED' AND o.cancelledAt >= :startDate AND o.cancelledAt <= :endDate")
    Long countCancelledOrdersInDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Đếm đơn có ghi chú nội bộ
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.internalNote IS NOT NULL AND o.internalNote <> ''")
    Long countOrdersWithInternalNotes();

    /**
     * Đếm đơn có ghi chú nội bộ được cập nhật trong khoảng thời gian (dựa vào updatedAt)
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.internalNote IS NOT NULL AND o.internalNote <> '' AND o.updatedAt >= :startDate AND o.updatedAt <= :endDate")
    Long countOrdersWithInternalNotesInDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    /**
     * Tính tổng điểm đã sử dụng
     */
    @Query("SELECT COALESCE(SUM(o.pointsUsed), 0) FROM Order o WHERE o.pointsUsed IS NOT NULL AND o.status NOT IN ('CANCELLED')")
    Long getTotalPointsUsed();

    /**
     * Tính tổng tiền giảm từ điểm
     */
    @Query("SELECT COALESCE(SUM(o.pointsDiscountAmount), 0) FROM Order o WHERE o.pointsDiscountAmount IS NOT NULL AND o.status NOT IN ('CANCELLED')")
    BigDecimal getTotalPointsDiscount();

    /**
     * Đếm đơn sử dụng coupon
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.couponCode IS NOT NULL AND o.couponCode <> '' AND o.status NOT IN ('CANCELLED')")
    Long countOrdersWithCoupon();

    /**
     * Tính tổng tiền giảm từ coupon
     */
    @Query("SELECT COALESCE(SUM(o.couponDiscountAmount), 0) FROM Order o WHERE o.couponDiscountAmount IS NOT NULL AND o.status NOT IN ('CANCELLED')")
    BigDecimal getTotalCouponDiscount();

    /**
     * Đếm đơn theo trạng thái thanh toán
     */
    Long countByPaymentStatus(PaymentStatus paymentStatus);

    /**
     * Tính giá trị đơn hàng trung bình
     */
    @Query("SELECT COALESCE(AVG(o.finalAmount), 0) FROM Order o WHERE o.status NOT IN ('CANCELLED')")
    BigDecimal getAverageOrderValue();

    // ============ ADVANCED STATISTICS QUERIES ============

    /**
     * Tính AOV (Average Order Value) trong khoảng thời gian - chỉ tính đơn COMPLETED
     */
    @Query("SELECT COALESCE(AVG(o.finalAmount), 0) FROM Order o " +
           "WHERE o.status = 'COMPLETED' " +
           "AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    BigDecimal getAOVByDateRange(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);

    /**
     * Đếm đơn hàng COMPLETED trong khoảng thời gian
     */
    @Query("SELECT COUNT(o) FROM Order o " +
           "WHERE o.status = 'COMPLETED' " +
           "AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    Long countCompletedOrdersInDateRange(@Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    /**
     * Đếm đơn hàng bị hủy trong khoảng thời gian (dựa vào createdAt)
     */
    @Query("SELECT COUNT(o) FROM Order o " +
           "WHERE o.status = 'CANCELLED' " +
           "AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    Long countCancelledOrdersByCreatedAtInDateRange(@Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Tính tổng điểm đã sử dụng trong khoảng thời gian
     */
    @Query("SELECT COALESCE(SUM(o.pointsUsed), 0) FROM Order o " +
           "WHERE o.pointsUsed IS NOT NULL " +
           "AND o.status NOT IN ('CANCELLED') " +
           "AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    Long getTotalPointsUsedInDateRange(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * Tính tổng tiền giảm từ điểm trong khoảng thời gian
     */
    @Query("SELECT COALESCE(SUM(o.pointsDiscountAmount), 0) FROM Order o " +
           "WHERE o.pointsDiscountAmount IS NOT NULL " +
           "AND o.status NOT IN ('CANCELLED') " +
           "AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    BigDecimal getTotalPointsDiscountInDateRange(@Param("startDate") LocalDateTime startDate,
                                                  @Param("endDate") LocalDateTime endDate);
}
