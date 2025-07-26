package com.foodorder.backend.order.repository;

import com.foodorder.backend.order.entity.OrderTracking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderTrackingRepository extends JpaRepository<OrderTracking, Long> {
    List<OrderTracking> findByOrderId(Long orderId);
}
