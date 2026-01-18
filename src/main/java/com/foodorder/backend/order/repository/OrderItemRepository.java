package com.foodorder.backend.order.repository;

import com.foodorder.backend.order.entity.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);

    @Query("SELECT oi FROM OrderItem oi LEFT JOIN FETCH oi.food WHERE oi.orderId = :orderId")
    List<OrderItem> findByOrderIdWithFood(@Param("orderId") Long orderId);

    // ============ DASHBOARD STATISTICS QUERIES ============

    /**
     * Lấy top món ăn bán chạy nhất trong khoảng thời gian (chỉ tính đơn COMPLETED)
     * Trả về: foodId, foodName, foodSlug, imageUrl, categoryName, quantitySold, revenue
     */
    @Query("SELECT oi.foodId, oi.foodName, oi.foodSlug, oi.imageUrl, " +
           "COALESCE(f.category.name, 'Không xác định'), " +
           "SUM(oi.quantity), SUM(oi.price * oi.quantity) " +
           "FROM OrderItem oi " +
           "LEFT JOIN oi.food f " +
           "JOIN oi.order o " +
           "WHERE o.status = 'COMPLETED' " +
           "AND o.createdAt >= :startDate AND o.createdAt <= :endDate " +
           "GROUP BY oi.foodId, oi.foodName, oi.foodSlug, oi.imageUrl, f.category.name " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> findTopSellingFoods(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate,
                                        Pageable pageable);

    /**
     * Lấy doanh thu theo danh mục trong khoảng thời gian (chỉ tính đơn COMPLETED)
     * Trả về: categoryId, categoryName, categorySlug, revenue, orderCount, quantitySold
     */
    @Query("SELECT f.category.id, f.category.name, f.category.slug, " +
           "SUM(oi.price * oi.quantity), COUNT(DISTINCT o.id), SUM(oi.quantity) " +
           "FROM OrderItem oi " +
           "JOIN oi.food f " +
           "JOIN oi.order o " +
           "WHERE o.status = 'COMPLETED' " +
           "AND o.createdAt >= :startDate AND o.createdAt <= :endDate " +
           "AND f.category IS NOT NULL " +
           "GROUP BY f.category.id, f.category.name, f.category.slug " +
           "ORDER BY SUM(oi.price * oi.quantity) DESC")
    List<Object[]> findRevenueByCategoryInDateRange(@Param("startDate") LocalDateTime startDate,
                                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Lấy chi tiết hiệu quả món ăn trong khoảng thời gian (chỉ tính đơn COMPLETED)
     * Trả về: foodId, foodName, foodSlug, imageUrl, categoryName, orderCount, quantitySold, revenue
     */
    @Query("SELECT oi.foodId, oi.foodName, oi.foodSlug, oi.imageUrl, " +
           "COALESCE(f.category.name, 'Không xác định'), " +
           "COUNT(DISTINCT o.id), SUM(oi.quantity), SUM(oi.price * oi.quantity) " +
           "FROM OrderItem oi " +
           "LEFT JOIN oi.food f " +
           "JOIN oi.order o " +
           "WHERE o.status = 'COMPLETED' " +
           "AND o.createdAt >= :startDate AND o.createdAt <= :endDate " +
           "GROUP BY oi.foodId, oi.foodName, oi.foodSlug, oi.imageUrl, f.category.name " +
           "ORDER BY SUM(oi.price * oi.quantity) DESC")
    List<Object[]> findFoodPerformanceInDateRange(@Param("startDate") LocalDateTime startDate,
                                                   @Param("endDate") LocalDateTime endDate,
                                                   Pageable pageable);

    /**
     * Đếm tổng số món ăn đã bán trong khoảng thời gian
     */
    @Query("SELECT COUNT(DISTINCT oi.foodId) FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE o.status = 'COMPLETED' " +
           "AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    Long countDistinctFoodsInDateRange(@Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    /**
     * Lấy doanh thu của một món ăn cụ thể trong khoảng thời gian
     */
    @Query("SELECT COALESCE(SUM(oi.price * oi.quantity), 0) FROM OrderItem oi " +
           "JOIN oi.order o " +
           "WHERE o.status = 'COMPLETED' " +
           "AND oi.foodId = :foodId " +
           "AND o.createdAt >= :startDate AND o.createdAt <= :endDate")
    BigDecimal getFoodRevenueInDateRange(@Param("foodId") Long foodId,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);
}
