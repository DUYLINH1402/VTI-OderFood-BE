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
import com.foodorder.backend.order.dto.OrderWebSocketMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.stream.Collectors;
import java.util.List;
import com.foodorder.backend.zone.repository.WardRepository;
import com.foodorder.backend.zone.repository.DistrictRepository;
import com.foodorder.backend.zone.entity.Ward;
import com.foodorder.backend.zone.entity.District;
import com.foodorder.backend.service.WebSocketService;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderTrackingRepository orderTrackingRepository;
    private final FoodRepository foodRepository;
    private final CouponService couponService;
    private final PointsService pointsService;
    private final UserRepository userRepository;
    private final WardRepository wardRepository;
    private final DistrictRepository districtRepository;
    private final WebSocketService webSocketService;

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

        // === BƯỚC 2: TÍNH TOÁN TIỀN TỆ ===
        BigDecimal subtotalAmount = orderRequest.getSubtotalAmount(); // Tổng tiền món ăn
        BigDecimal shippingFee = orderRequest.getShippingFee() != null ? orderRequest.getShippingFee() : BigDecimal.ZERO;
        BigDecimal totalBeforeDiscount = subtotalAmount.add(shippingFee); // Tổng trước giảm giá

        // Khởi tạo finalAmount = totalBeforeDiscount
        BigDecimal finalAmount = totalBeforeDiscount;

        // Khởi tạo các biến giảm giá
        String appliedCouponCode = null;
        BigDecimal couponDiscountAmount = BigDecimal.ZERO;
        BigDecimal pointsDiscountAmount = BigDecimal.ZERO;
        Integer pointsUsed = 0;

        // === BƯỚC 3: VALIDATE VÀ ÁP DỤNG COUPON NẾU CÓ ===
        if (orderRequest.getCouponCode() != null && !orderRequest.getCouponCode().trim().isEmpty()) {
            // Lấy danh sách foodIds từ request
            List<Long> foodIds = orderRequest.getItems().stream()
                    .map(OrderItemRequest::getFoodId)
                    .toList();

            // Tạo coupon request
            ApplyCouponRequest couponRequest = new ApplyCouponRequest();
            couponRequest.setCouponCode(orderRequest.getCouponCode().trim());
            couponRequest.setUserId(orderRequest.getUserId());
            couponRequest.setOrderAmount(totalBeforeDiscount.doubleValue());
            couponRequest.setFoodIds(foodIds);

            // Validate coupon
            CouponApplyResult couponResult = couponService.validateCouponForOrder(couponRequest);

            if (!Boolean.TRUE.equals(couponResult.getSuccess())) {
                throw new BadRequestException("Coupon không hợp lệ: " + couponResult.getMessage(), "COUPON_INVALID");
            }

            // Áp dụng coupon thành công
            appliedCouponCode = orderRequest.getCouponCode().trim();
            couponDiscountAmount = BigDecimal.valueOf(couponResult.getDiscountAmount());
            finalAmount = finalAmount.subtract(couponDiscountAmount);
        }

        // === BƯỚC 4: VALIDATE VÀ ÁP DỤNG ĐIỂM THƯỞNG NẾU CÓ ===
        if (orderRequest.getPointsUsed() != null && orderRequest.getPointsUsed() > 0) {
            if (orderRequest.getUserId() == null) {
                throw new BadRequestException(
                        "Không thể sử dụng điểm thưởng cho đơn hàng khách vãng lai",
                        "POINTS_GUEST_ORDER");
            }

            try {
                // Lấy username từ userId
                String username = userRepository.findById(orderRequest.getUserId())
                        .orElseThrow(() -> new BadRequestException(
                                "Không tìm thấy thông tin người dùng",
                                "USER_NOT_FOUND"))
                        .getUsername();

                // Lấy số điểm hiện tại của user
                PointsResponseDTO pointsDTO = pointsService.getCurrentPointsByUsername(username);
                int currentPoints = pointsDTO != null ? pointsDTO.getAvailablePoints() : 0;

                // Kiểm tra đủ điểm hay không
                pointsUsed = orderRequest.getPointsUsed();
                if (currentPoints < pointsUsed) {
                    throw new BadRequestException(
                            "Không đủ điểm thưởng. Cần " + pointsUsed + " điểm, hiện có " + currentPoints + " điểm",
                            "INSUFFICIENT_POINTS");
                }

                // Tính số tiền giảm từ điểm (giả sử 1000 điểm = 1000 VND)
                pointsDiscountAmount = BigDecimal.valueOf(pointsUsed * 1L);

                // Kiểm tra không được giảm quá số tiền phải trả
                if (pointsDiscountAmount.compareTo(finalAmount) > 0) {
                    throw new BadRequestException(
                            "Số điểm sử dụng vượt quá giá trị đơn hàng",
                            "POINTS_EXCEED_ORDER_VALUE");
                }

                // Áp dụng giảm giá từ điểm
                finalAmount = finalAmount.subtract(pointsDiscountAmount);

            } catch (BadRequestException e) {
                throw e; // Ném lại BadRequestException
            } catch (Exception e) {
                throw new BadRequestException("Lỗi kiểm tra điểm thưởng: " + e.getMessage(), "POINTS_VALIDATION_ERROR");
            }
        }

        // Đảm bảo finalAmount không âm
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        // === BƯỚC 5: TẠO ORDER VỚI THÔNG TIN ĐÃ VALIDATE ===
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
                .districtId(orderRequest.getDistrictId())
                .wardId(orderRequest.getWardId())

                // === TIỀN TỆ MỚI - RÕ RÀNG ===
                .subtotalAmount(subtotalAmount)
                .shippingFee(shippingFee)
                .totalBeforeDiscount(totalBeforeDiscount)
                .finalAmount(finalAmount)

                // === GIẢM GIÁ ===
                .pointsUsed(pointsUsed)
                .pointsDiscountAmount(pointsDiscountAmount)
                .couponCode(appliedCouponCode)
                .couponDiscountAmount(couponDiscountAmount)

                // === DEPRECATED FIELDS - GIỮ LẠI ĐỂ TƯƠNG THÍCH ===
                .totalFoodPrice(subtotalAmount) // Map với subtotalAmount
                .totalPrice(finalAmount) // Map với finalAmount
                .discountAmount(pointsUsed) // Map với pointsUsed
                .originalAmount(totalBeforeDiscount) // Map với totalBeforeDiscount
                .build();

        order = orderRepository.save(order);

        // === BƯỚC 6: TẠO ORDER ITEMS ===
        final Order finalOrder = order;
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
                .toList();

        // === BƯỚC 7: TẠO ORDER TRACKING ===
        OrderTracking tracking = OrderTracking.builder()
                .orderId(order.getId())
                .status(OrderTrackingStatus.PENDING)
                .build();
        orderTrackingRepository.save(tracking);

        // === BƯỚC 8: RETURN RESPONSE ===
        return OrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
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

                // === TIỀN TỆ MỚI - RÕ RÀNG ===
                .subtotalAmount(subtotalAmount)
                .shippingFee(shippingFee)
                .totalBeforeDiscount(totalBeforeDiscount)
                .finalAmount(finalAmount)

                // === GIẢM GIÁ ===
                .pointsUsed(pointsUsed)
                .pointsDiscountAmount(pointsDiscountAmount)
                .couponCode(appliedCouponCode)
                .couponDiscountAmount(couponDiscountAmount)

                .createdAt(order.getCreatedAt())
                .items(orderItems.stream().map(item -> OrderResponse.OrderItemResponse.builder()
                        .foodId(item.getFoodId())
                        .foodName(item.getFoodName())
                        .foodSlug(item.getFoodSlug())
                        .imageUrl(item.getImageUrl())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build()).toList())
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
            // Lưu trạng thái cũ để gửi notification
            String previousStatus = order.getStatus().name();

            OrderStatus newStatus = OrderStatus.valueOf(request.getStatus().toUpperCase());
            order.setStatus(newStatus);
            orderRepository.save(order);

            // Cập nhật order tracking
            OrderTracking tracking = OrderTracking.builder()
                    .orderId(order.getId())
                    .status(OrderTrackingStatus.valueOf(request.getStatus().toUpperCase()))
                    .build();
            orderTrackingRepository.save(tracking);

            // === GỬI WEBSOCKET NOTIFICATION ===
            try {

                // Tạo message riêng cho customer với nội dung thân thiện
                OrderWebSocketMessage customerMessage = OrderWebSocketMessage.customerNotification(
                    order.getId(),
                    order.getOrderCode() != null ? order.getOrderCode() : "ORD" + order.getId(),
                    newStatus.name(),
                    previousStatus,
                    userId
                );

                // Gửi thông báo riêng cho user cụ thể
                webSocketService.sendNotificationToUser(userId, customerMessage);

            } catch (Exception e) {
                // Log lỗi nhưng không throw exception
                System.err.println("Error sending WebSocket notification for order status update: " + e.getMessage());
            }

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
        long confirmedOrders = orderRepository.countByUserIdAndStatus(userId, OrderStatus.CONFIRMED);
        long completedOrders = orderRepository.countByUserIdAndStatus(userId, OrderStatus.COMPLETED);
        long cancelledOrders = orderRepository.countByUserIdAndStatus(userId, OrderStatus.CANCELLED);

        // Sử dụng BigDecimal thay vì Double để thống nhất
        BigDecimal totalSpent = orderRepository.getTotalSpentByUserId(userId);
        if (totalSpent == null)
            totalSpent = BigDecimal.ZERO;

        // Tính averageOrderValue với BigDecimal - sử dụng RoundingMode thay vì
        // deprecated constant
        BigDecimal averageOrderValue = BigDecimal.ZERO;
        if (totalOrders > 0) {
            averageOrderValue = totalSpent.divide(BigDecimal.valueOf(totalOrders), 2, java.math.RoundingMode.HALF_UP);
        }

        return OrderStatisticsResponse.builder()
                .totalOrders(totalOrders)
                .confirmedOrders(pendingOrders)
                .preparingOrders(confirmedOrders)
                .shippingOrders(0L) // Không có status shipping trong enum hiện tại
                .deliveredOrders(completedOrders)
                .cancelledOrders(cancelledOrders)
                .totalSpent(totalSpent) // Sử dụng BigDecimal
                .averageOrderValue(averageOrderValue) // Sử dụng BigDecimal
                .build();
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getId());

        // Lấy thông tin Ward và District name từ DB
        String wardName = null;
        String districtName = null;

        if (order.getWardId() != null) {
            Ward ward = wardRepository.findById(order.getWardId()).orElse(null);
            if (ward != null) {
                wardName = ward.getName();
            }
        }

        if (order.getDistrictId() != null) {
            District district = districtRepository.findById(order.getDistrictId()).orElse(null);
            if (district != null) {
                districtName = district.getName();
            }
        }

        return OrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .userId(order.getUserId())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .receiverEmail(order.getReceiverEmail())
                .deliveryAddress(order.getDeliveryAddress())
                .deliveryType(order.getDeliveryType())
                .paymentMethod(order.getPaymentMethod().name())
                .districtId(order.getDistrictId())
                .districtName(districtName)
                .wardId(order.getWardId())
                .wardName(wardName)
                .status(order.getStatus().name())
                .paymentStatus(order.getPaymentStatus().name())

                // === TIỀN TỆ MỚI - RÕ RÀNG ===
                .subtotalAmount(order.getSubtotalAmount())
                .shippingFee(order.getShippingFee())
                .totalBeforeDiscount(order.getTotalBeforeDiscount())
                .finalAmount(order.getFinalAmount())

                // === GIẢM GIÁ ===
                .pointsUsed(order.getPointsUsed())
                .pointsDiscountAmount(order.getPointsDiscountAmount())
                .couponCode(order.getCouponCode())
                .couponDiscountAmount(order.getCouponDiscountAmount())

                // === DEPRECATED FIELDS - GIỮ LẠI ĐỂ TƯƠNG THÍCH ===
                .discountAmount(order.getDiscountAmount())

                .createdAt(order.getCreatedAt())
                .staffNote(order.getStaffNote())
                .items(orderItems.stream().map(item -> OrderResponse.OrderItemResponse.builder()
                        .foodId(item.getFoodId())
                        .foodName(item.getFoodName())
                        .foodSlug(item.getFoodSlug())
                        .imageUrl(item.getImageUrl())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build()).toList())
                .build();
    }
}
