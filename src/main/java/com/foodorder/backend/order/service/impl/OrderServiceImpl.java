package com.foodorder.backend.order.service.impl;

import com.foodorder.backend.exception.BadRequestException;
import com.foodorder.backend.food.entity.Food;
import com.foodorder.backend.food.repository.FoodRepository;
import com.foodorder.backend.order.dto.request.*;
import com.foodorder.backend.order.dto.response.OrderResponse;
import com.foodorder.backend.order.dto.response.PageResponse;
import com.foodorder.backend.order.dto.response.OrderStatisticsResponse;
import com.foodorder.backend.order.entity.*;
import com.foodorder.backend.order.repository.*;
import com.foodorder.backend.order.service.OrderService;
import com.foodorder.backend.coupons.service.CouponService;
import com.foodorder.backend.coupons.dto.request.ApplyCouponRequest;
import com.foodorder.backend.coupons.dto.response.CouponApplyResult;
import com.foodorder.backend.points.service.PointsService;
import com.foodorder.backend.points.dto.response.PointsResponseDTO;
import com.foodorder.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.stream.Collectors;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderTrackingRepository orderTrackingRepository;
    private final FoodRepository foodRepository;
    private final CouponService couponService;
    private final PointsService pointsService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest) {
        // === BƯỚC 1: VALIDATE CƠ BẢN ===
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

        // === BƯỚC 2: VALIDATE COUPON TRƯỚC KHI LƯU ORDER ===
        BigDecimal finalAmount = orderRequest.getTotalPrice();
        BigDecimal originalAmount = orderRequest.getTotalPrice();
        String appliedCouponCode = null;
        BigDecimal couponDiscountAmount = null;

        // Kiểm tra coupon nếu có
        if (orderRequest.getCouponCode() != null && !orderRequest.getCouponCode().trim().isEmpty()) {
            System.out.println("Validating coupon: " + orderRequest.getCouponCode());

            // Lấy danh sách foodIds từ request
            List<Long> foodIds = orderRequest.getItems().stream()
                .map(OrderItemRequest::getFoodId)
                .collect(Collectors.toList());

            // Tạo coupon request
            ApplyCouponRequest couponRequest = new ApplyCouponRequest();
            couponRequest.setCouponCode(orderRequest.getCouponCode().trim());
            couponRequest.setUserId(orderRequest.getUserId());
            couponRequest.setOrderAmount(orderRequest.getTotalPrice().doubleValue());
            couponRequest.setFoodIds(foodIds);

            // Validate coupon
            CouponApplyResult couponResult = couponService.validateCouponForOrder(couponRequest);
            System.out.println("CouponCode: " + couponRequest.getCouponCode());
            System.out.println("UserId: " + couponRequest.getUserId());
            System.out.println("OrderAmount: " + couponRequest.getOrderAmount());
            System.out.println("FoodIds: " + couponRequest.getFoodIds());
            if (!Boolean.TRUE.equals(couponResult.getSuccess())) {
                throw new BadRequestException("Coupon code not found: " + couponResult.getMessage(), "COUPON_INVALID_");
            }

            // Áp dụng coupon thành công
            appliedCouponCode = orderRequest.getCouponCode().trim();
            couponDiscountAmount = BigDecimal.valueOf(couponResult.getDiscountAmount());
            originalAmount = orderRequest.getTotalPrice(); // Số tiền gốc
            finalAmount = originalAmount.subtract(couponDiscountAmount); // Số tiền sau giảm giá

            System.out.println("Coupon applied successfully: " + appliedCouponCode +
                              ", discount: " + couponDiscountAmount +
                              ", final amount: " + finalAmount);
        }

        // === BƯỚC 3: VALIDATE ĐIỂM THƯỞNG NẾU CÓ ===
        if (orderRequest.getDiscountAmount() != null && orderRequest.getDiscountAmount() > 0) {
            validatePointsUsage(orderRequest);
        }

        // === BƯỚC 4: TẠO ORDER VỚI THÔNG TIN ĐÃ VALIDATE ===
        System.out.println("Creating order...");
        Order order = Order.builder()
                .userId(orderRequest.getUserId())
                .receiverName(orderRequest.getReceiverName())
                .receiverPhone(orderRequest.getReceiverPhone())
                .receiverEmail(orderRequest.getReceiverEmail())
                .deliveryAddress(orderRequest.getDeliveryAddress())
                .paymentMethod(orderRequest.getPaymentMethod())
                .deliveryType(orderRequest.getDeliveryType())
                .status(OrderStatus.PENDING)
                .paymentStatus(PaymentStatus.PENDING)
                .totalPrice(finalAmount) // Số tiền cuối cùng sau khi áp dụng coupon
                .discountAmount(orderRequest.getDiscountAmount()) // Discount từ points
                .districtId(orderRequest.getDistrictId())
                .wardId(orderRequest.getWardId())
                // Thêm thông tin coupon đã validate
                .couponCode(appliedCouponCode)
                .couponDiscountAmount(couponDiscountAmount)
                .originalAmount(originalAmount)
                .build();

        order = orderRepository.save(order);

        // === BƯỚC 5: TẠO ORDER ITEMS ===
        final Order finalOrder = order; // Tạo final reference cho lambda
        var orderItems = orderRequest.getItems().stream()
                .map(itemReq -> {
                    Food food = foodRepository.findById(itemReq.getFoodId())
                            .orElseThrow(() -> new IllegalArgumentException("FOOD_NOT_FOUND: " + itemReq.getFoodId()));
                    OrderItem item = OrderItem.builder()
                            .orderId(finalOrder.getId())
                            .order(finalOrder)
                            .foodId(itemReq.getFoodId())
                            .food(food)
                            .quantity(itemReq.getQuantity())
                            .price(itemReq.getPrice())
                            .foodName(food.getName())
                            .foodSlug(food.getSlug())
                            .imageUrl(food.getImageUrl())
                            .build();
                    return orderItemRepository.save(item);
                })
                .collect(Collectors.toList());

        // === BƯỚC 6: TẠO ORDER TRACKING ===
        OrderTracking tracking = OrderTracking.builder()
                .orderId(order.getId())
                .status(OrderTrackingStatus.PENDING)
                .build();
        orderTrackingRepository.save(tracking);

        // === BƯỚC 7: TRẢ VỀ RESPONSE ===
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
                .totalPrice(order.getTotalPrice()) // Số tiền cuối cùng
                .discountAmount(orderRequest.getDiscountAmount())
                // Thêm thông tin coupon vào response
                .couponCode(appliedCouponCode)
                .couponDiscountAmount(couponDiscountAmount != null ? couponDiscountAmount.doubleValue() : null)
                .originalAmount(originalAmount != null ? originalAmount.doubleValue() : null)
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

    /**
     * Validate việc sử dụng điểm thưởng
     */
    private void validatePointsUsage(OrderRequest orderRequest) {
        if (orderRequest.getUserId() == null) {
            throw new BadRequestException("Không thể áp dụng coupon cho đơn hàng không có userId (khách chưa đăng nhập)", "COUPON_USERID_NULL");
        }

        try {
            // Lấy username từ userId
            String username = userRepository.findById(orderRequest.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user với id: " + orderRequest.getUserId()))
                .getUsername();

            // Lấy số điểm hiện tại của user
            PointsResponseDTO pointsDTO = pointsService.getCurrentPointsByUsername(username);
            int currentPoints = pointsDTO != null ? pointsDTO.getAvailablePoints() : 0;

            // Tính số điểm cần dùng (giả sử 1 điểm = 1000 VND)
            int pointsNeeded = orderRequest.getDiscountAmount() / 1000;

            if (currentPoints < pointsNeeded) {
                throw new IllegalArgumentException("Không đủ điểm thưởng. Cần " + pointsNeeded + " điểm, hiện có " + currentPoints + " điểm");
            }

            System.out.println("Points validation successful: using " + pointsNeeded + " points");
        } catch (Exception e) {
            throw new IllegalArgumentException("Lỗi kiểm tra điểm thưởng: " + e.getMessage());
        }
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
                .discountAmount(order.getDiscountAmount())
                // Thêm thông tin coupon
                .couponCode(order.getCouponCode())
                .couponDiscountAmount(order.getCouponDiscountAmount() != null ?
                    order.getCouponDiscountAmount().doubleValue() : null)
                .originalAmount(order.getOriginalAmount() != null ?
                    order.getOriginalAmount().doubleValue() : null)
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
