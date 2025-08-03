package com.foodorder.backend.payments.service.impl;

import com.foodorder.backend.exception.ResourceNotFoundException;
import com.foodorder.backend.order.entity.Order;
import com.foodorder.backend.order.entity.OrderItem;
import com.foodorder.backend.order.entity.OrderStatus;
import com.foodorder.backend.order.repository.OrderRepository;
import com.foodorder.backend.order.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public abstract class BasePaymentService {

    protected final OrderRepository orderRepository;
    protected final OrderItemRepository orderItemRepository;

    /**
     * Lấy Order với Items từ DB (logic chung cho tất cả payment methods)
     */
    protected Order getOrderWithItems(Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId);
        if (order == null) {
            throw new ResourceNotFoundException("Order not found with id: " + orderId, "ORDER_NOT_FOUND");
        }

        // Validate order status
        if (order.getStatus() == null || !order.getStatus().equals(OrderStatus.PENDING)) {
            throw new IllegalStateException("Order is not in PENDING status. Current status: " + order.getStatus());
        }

        return order;
    }

    /**
     * Lấy OrderItems (với fallback nếu lazy loading fail)
     */
    protected List<OrderItem> getOrderItems(Order order) {
        List<OrderItem> orderItems;

        if (order.getItems() != null && !order.getItems().isEmpty()) {
            orderItems = order.getItems();
        } else {
            // Fallback: query trực tiếp từ DB
            orderItems = orderItemRepository.findByOrderIdWithFood(order.getId());
        }

        if (orderItems.isEmpty()) {
            throw new IllegalStateException("Order has no items");
        }

        return orderItems;
    }

    /**
     * Generate transaction ID theo format chung
     */
    protected String generateTransactionId(String prefix, Long orderId) {
        return prefix + "_" + System.currentTimeMillis() + "_" + orderId;
    }
}
