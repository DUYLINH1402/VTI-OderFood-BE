package com.foodorder.backend.order.repository;

import com.foodorder.backend.order.entity.Order;
import com.foodorder.backend.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
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
    Double getTotalSpentByUserId(@Param("userId") Long userId);
}
