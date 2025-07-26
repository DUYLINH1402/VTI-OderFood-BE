package com.foodorder.backend.order.repository;

import com.foodorder.backend.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);

    @Query("SELECT oi FROM OrderItem oi LEFT JOIN FETCH oi.food WHERE oi.orderId = :orderId")
    List<OrderItem> findByOrderIdWithFood(@Param("orderId") Long orderId);
}
