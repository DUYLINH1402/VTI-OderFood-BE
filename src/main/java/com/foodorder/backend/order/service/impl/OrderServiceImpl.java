package com.foodorder.backend.order.service.impl;

import com.foodorder.backend.food.entity.Food;
import com.foodorder.backend.food.repository.FoodRepository;
import com.foodorder.backend.order.dto.request.*;
import com.foodorder.backend.order.dto.response.OrderResponse;
import com.foodorder.backend.order.dto.response.PageResponse;
import com.foodorder.backend.order.dto.response.OrderStatisticsResponse;
import com.foodorder.backend.order.entity.*;
import com.foodorder.backend.order.repository.*;
import com.foodorder.backend.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderTrackingRepository orderTrackingRepository;
    private final FoodRepository foodRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest) {
        // Validate items first
        if (orderRequest.getItems() == null || orderRequest.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order items cannot be null or empty");
        }

        // Validate theo deliveryType
        if (orderRequest.getDeliveryType() == DeliveryType.DELIVERY) {
            if (orderRequest.getDeliveryAddress() == null || orderRequest.getDeliveryAddress().trim().isEmpty()) {
                throw new IllegalArgumentException("deliveryAddress is required for DELIVERY");
            }
            if (orderRequest.getDistrictId() == null) {
                throw new IllegalArgumentException("districtId is required for DELIVERY");
            }
            if (orderRequest.getWardId() == null) {
                throw new IllegalArgumentException("wardId is required for DELIVERY");
            }
        }

        // 1. Create Order
        System.out.println("Creating order...");
        Order tempOrder = Order.builder()
                .userId(orderRequest.getUserId())
                .receiverName(orderRequest.getReceiverName())
                .receiverPhone(orderRequest.getReceiverPhone())
                .receiverEmail(orderRequest.getReceiverEmail())
                .deliveryAddress(orderRequest.getDeliveryAddress())
                .paymentMethod(orderRequest.getPaymentMethod())
                .deliveryType(orderRequest.getDeliveryType())
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .totalPrice(orderRequest.getTotalPrice())
                .districtId(orderRequest.getDistrictId())
                .wardId(orderRequest.getWardId())
                .build();
        Order order = orderRepository.save(tempOrder);

        // 2. Create Order Items
        var orderItems = orderRequest.getItems().stream()
                .map(itemReq -> {
                    Food food = foodRepository.findById(itemReq.getFoodId())
                            .orElseThrow(() -> new IllegalArgumentException("FOOD_NOT_FOUND: " + itemReq.getFoodId()));
                    OrderItem item = OrderItem.builder()
                            .orderId(order.getId())
                            .order(order) // Thêm dòng này để set order object
                            .foodId(itemReq.getFoodId())
                            .food(food) // Cũng set food object để đảm bảo
                            .quantity(itemReq.getQuantity())
                            .price(itemReq.getPrice())
                            .foodName(food.getName()) // BE tự lưu tên món ăn thêm
                            .foodSlug(food.getSlug())
                            .imageUrl(food.getImageUrl())
                            .build();
                    return orderItemRepository.save(item);
                })
                .collect(Collectors.toList());

        // 3. Create Order Tracking
        OrderTracking tracking = OrderTracking.builder()
                .orderId(order.getId())
                .status(OrderTrackingStatus.PENDING)
                .build();
        orderTrackingRepository.save(tracking);

        // 4. Map to Response
        return OrderResponse.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .receiverEmail(order.getReceiverEmail())
                .deliveryAddress(order.getDeliveryAddress())
                .deliveryType(orderRequest.getDeliveryType())
                .paymentMethod(order.getPaymentMethod().name())
                .districtId(order.getDistrictId())
                .wardId(order.getWardId())
                .status(order.getStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .items(orderItems.stream().map(item -> OrderResponse.OrderItemResponse.builder()
                        .foodId(item.getFoodId())
                        .foodName(item.getFoodName()) // lấy từ order_items
                        .foodSlug(item.getFoodSlug())
                        .imageUrl(item.getImageUrl())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build()).collect(Collectors.toList()))
                .build();

    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getOrdersByUser(Long userId, String status, PageRequest pageRequest) {
        Page<Order> orders;

        if ("all".equals(status)) {
            orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageRequest);
        } else {
            try {
                OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
                orders = orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, orderStatus, pageRequest);
            } catch (IllegalArgumentException e) {
                // Nếu status không hợp lệ, trả về tất cả orders
                orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId, pageRequest);
            }
        }

        List<OrderResponse> orderResponses = orders.getContent().stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());

        return PageResponse.<OrderResponse>builder()
                .data(orderResponses)
                .page(orders.getNumber())
                .size(orders.getSize())
                .total(orders.getTotalElements())
                .totalPages(orders.getTotalPages())
                .hasNext(orders.hasNext())
                .hasPrevious(orders.hasPrevious())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderDetail(String orderCode, Long userId) {
        Order order = orderRepository.findByIdAndUserId(Long.parseLong(orderCode), userId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        return mapToOrderResponse(order);
    }

    @Override
    @Transactional
    public void updateOrderStatus(String orderCode, Long userId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findByIdAndUserId(Long.parseLong(orderCode), userId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        try {
            OrderStatus newStatus = OrderStatus.valueOf(request.getStatus().toUpperCase());
            order.setStatus(newStatus);
            orderRepository.save(order);

            // Cập nhật order tracking
            OrderTracking tracking = OrderTracking.builder()
                    .orderId(order.getId())
                    .status(OrderTrackingStatus.valueOf(request.getStatus().toUpperCase()))
                    .build();
            orderTrackingRepository.save(tracking);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid order status: " + request.getStatus());
        }
    }

    @Override
    @Transactional
    public void cancelOrder(String orderCode, Long userId, String cancelReason) {
        Order order = orderRepository.findByIdAndUserId(Long.parseLong(orderCode), userId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new IllegalArgumentException("Cannot cancel completed order");
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        // Cập nhật order tracking
        OrderTracking tracking = OrderTracking.builder()
                .orderId(order.getId())
                .status(OrderTrackingStatus.CANCELLED)
                .build();
        orderTrackingRepository.save(tracking);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderStatisticsResponse getOrderStatistics(Long userId) {
        long totalOrders = orderRepository.countByUserId(userId);
        long pendingOrders = orderRepository.countByUserIdAndStatus(userId, OrderStatus.PENDING);
        long processingOrders = orderRepository.countByUserIdAndStatus(userId, OrderStatus.PROCESSING);
        long completedOrders = orderRepository.countByUserIdAndStatus(userId, OrderStatus.COMPLETED);
        long cancelledOrders = orderRepository.countByUserIdAndStatus(userId, OrderStatus.CANCELLED);

        Double totalSpent = orderRepository.getTotalSpentByUserId(userId);
        if (totalSpent == null)
            totalSpent = 0.0;

        double averageOrderValue = totalOrders > 0 ? totalSpent / totalOrders : 0.0;

        return OrderStatisticsResponse.builder()
                .totalOrders(totalOrders)
                .confirmedOrders(pendingOrders)
                .preparingOrders(processingOrders)
                .shippingOrders(0L) // Không có status shipping trong enum hiện tại
                .deliveredOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .totalSpent(totalSpent)
                .averageOrderValue(averageOrderValue)
                .build();
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());

        return OrderResponse.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .receiverEmail(order.getReceiverEmail())
                .deliveryAddress(order.getDeliveryAddress())
                .deliveryType(order.getDeliveryType())
                .paymentMethod(order.getPaymentMethod().name())
                .districtId(order.getDistrictId())
                .wardId(order.getWardId())
                .status(order.getStatus().name())
                .paymentStatus(order.getPaymentStatus().name())
                .totalPrice(order.getTotalPrice())
                .createdAt(order.getCreatedAt())
                .items(orderItems.stream().map(item -> OrderResponse.OrderItemResponse.builder()
                        .foodId(item.getFoodId())
                        .foodName(item.getFoodName())
                        .foodSlug(item.getFoodSlug())
                        .imageUrl(item.getImageUrl())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build()).collect(Collectors.toList()))
                .build();
    }
}
