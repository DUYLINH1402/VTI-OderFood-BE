package com.foodorder.backend.order.util;

import com.foodorder.backend.order.dto.response.OrderResponse;
import com.foodorder.backend.order.entity.Order;
import com.foodorder.backend.order.entity.OrderItem;
import com.foodorder.backend.zone.entity.District;
import com.foodorder.backend.zone.entity.Ward;
import com.foodorder.backend.zone.repository.DistrictRepository;
import com.foodorder.backend.zone.repository.WardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper để chuyển đổi giữa Order entity và OrderResponse DTO
 */
@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final DistrictRepository districtRepository;
    private final WardRepository wardRepository;

    /**
     * Chuyển đổi Order entity thành OrderResponse DTO (Version thường - single order)
     */
    public OrderResponse toOrderResponse(Order order) {
        if (order == null) {
            return null;
        }

        // Lấy thông tin district và ward
        String districtName = getDistrictName(order.getDistrictId());
        String wardName = getWardName(order.getWardId());

        // Mapping danh sách OrderItem thành OrderItemResponse
        List<OrderResponse.OrderItemResponse> itemResponses = mapOrderItems(order.getItems());

        return OrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .userId(order.getUserId())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .receiverEmail(order.getReceiverEmail())
                .deliveryAddress(order.getDeliveryAddress())
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
                .deliveryType(order.getDeliveryType())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                // === TIỀN TỆ MỚI ===
                .subtotalAmount(order.getSubtotalAmount())
                .shippingFee(order.getShippingFee())
                .totalBeforeDiscount(order.getTotalBeforeDiscount() != null ? order.getTotalBeforeDiscount() : order.getOriginalAmount())
                .finalAmount(order.getFinalAmount())
                // === GIẢM GIÁ ===
                .pointsUsed(order.getPointsUsed())
                .pointsDiscountAmount(order.getPointsDiscountAmount())
                .couponCode(order.getCouponCode())
                .couponDiscountAmount(order.getCouponDiscountAmount())
                .discountAmount(order.getDiscountAmount())
                // === THỜI GIAN ===
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                // === ĐỊA CHỈ ===
                .districtId(order.getDistrictId())
                .districtName(districtName)
                .wardId(order.getWardId())
                .wardName(wardName)
                // === THANH TOÁN ===
                .paymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null)
                .paymentTime(order.getPaymentTime())
                .paymentTransactionId(order.getPaymentTransactionId())
                // === MANAGEMENT FIELDS ===
                .staffNote(order.getStaffNote())
                .internalNote(order.getInternalNote())
                .cancelReason(order.getCancelReason())
                .cancelledAt(order.getCancelledAt())
                .items(itemResponses)
                .build();
    }

    /**
     * Chuyển đổi danh sách Order entities thành danh sách OrderResponse DTOs (Version tối ưu - batch)
     */
    public List<OrderResponse> toOrderResponseList(List<Order> orders) {
        if (orders == null || orders.isEmpty()) {
            return List.of();
        }

        // Lấy tất cả district IDs và ward IDs từ danh sách orders
        Set<Long> districtIds = orders.stream()
                .map(Order::getDistrictId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        Set<Long> wardIds = orders.stream()
                .map(Order::getWardId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());

        // Batch load districts và wards để tránh N+1 query problem
        Map<Long, String> districtNameMap = loadDistrictNames(districtIds);
        Map<Long, String> wardNameMap = loadWardNames(wardIds);

        // Map từng order sử dụng pre-loaded maps
        return orders.stream()
                .map(order -> toOrderResponseOptimized(order, districtNameMap, wardNameMap))
                .collect(Collectors.toList());
    }

    /**
     * Lấy tên district từ ID (cho single order)
     */
    private String getDistrictName(Long districtId) {
        if (districtId == null) {
            return null;
        }
        return districtRepository.findById(districtId)
                .map(District::getName)
                .orElse(null);
    }

    /**
     * Lấy tên ward từ ID (cho single order)
     */
    private String getWardName(Long wardId) {
        if (wardId == null) {
            return null;
        }
        return wardRepository.findById(wardId)
                .map(Ward::getName)
                .orElse(null);
    }

    /**
     * Batch load district names để tối ưu performance (cho multiple orders)
     */
    private Map<Long, String> loadDistrictNames(Set<Long> districtIds) {
        if (districtIds.isEmpty()) {
            return Map.of();
        }

        return districtRepository.findAllById(districtIds).stream()
                .collect(Collectors.toMap(
                        District::getId,
                        District::getName
                ));
    }

    /**
     * Batch load ward names để tối ưu performance (cho multiple orders)
     */
    private Map<Long, String> loadWardNames(Set<Long> wardIds) {
        if (wardIds.isEmpty()) {
            return Map.of();
        }

        return wardRepository.findAllById(wardIds).stream()
                .collect(Collectors.toMap(
                        Ward::getId,
                        Ward::getName
                ));
    }

    /**
     * Version tối ưu của toOrderResponse sử dụng pre-loaded maps
     */
    private OrderResponse toOrderResponseOptimized(Order order,
                                                   Map<Long, String> districtNameMap,
                                                   Map<Long, String> wardNameMap) {

        String districtName = order.getDistrictId() != null ?
                districtNameMap.get(order.getDistrictId()) : null;
        String wardName = order.getWardId() != null ?
                wardNameMap.get(order.getWardId()) : null;

        List<OrderResponse.OrderItemResponse> itemResponses = mapOrderItems(order.getItems());

        return OrderResponse.builder()
                .id(order.getId())
                .orderCode(order.getOrderCode())
                .userId(order.getUserId())
                .receiverName(order.getReceiverName())
                .receiverPhone(order.getReceiverPhone())
                .receiverEmail(order.getReceiverEmail())
                .deliveryAddress(order.getDeliveryAddress())
                .paymentMethod(order.getPaymentMethod() != null ? order.getPaymentMethod().name() : null)
                .deliveryType(order.getDeliveryType())
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                // === TIỀN TỆ MỚI ===
                .subtotalAmount(order.getSubtotalAmount())
                .shippingFee(order.getShippingFee())
                .totalBeforeDiscount(order.getTotalBeforeDiscount() != null ? order.getTotalBeforeDiscount() : order.getOriginalAmount())
                .finalAmount(order.getFinalAmount())
                // === GIẢM GIÁ ===
                .pointsUsed(order.getPointsUsed())
                .pointsDiscountAmount(order.getPointsDiscountAmount())
                .couponCode(order.getCouponCode())
                .couponDiscountAmount(order.getCouponDiscountAmount())
                .discountAmount(order.getDiscountAmount())
                // === THỜI GIAN ===
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                // === ĐỊA CHỈ ===
                .districtId(order.getDistrictId())
                .districtName(districtName)
                .wardId(order.getWardId())
                .wardName(wardName)
                // === THANH TOÁN ===
                .paymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null)
                .paymentTime(order.getPaymentTime())
                .paymentTransactionId(order.getPaymentTransactionId())
                // === MANAGEMENT FIELDS ===
                .staffNote(order.getStaffNote())
                .internalNote(order.getInternalNote())
                .cancelReason(order.getCancelReason())
                .cancelledAt(order.getCancelledAt())
                .items(itemResponses)
                .build();
    }

    /**
     * Chuyển đổi OrderItem entities thành OrderItemResponse DTOs
     */
    private List<OrderResponse.OrderItemResponse> mapOrderItems(List<OrderItem> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            return List.of();
        }

        return orderItems.stream()
                .map(this::toOrderItemResponse)
                .collect(Collectors.toList());
    }

    /**
     * Chuyển đổi OrderItem entity thành OrderItemResponse DTO
     */
    private OrderResponse.OrderItemResponse toOrderItemResponse(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }

        return OrderResponse.OrderItemResponse.builder()
                .foodId(orderItem.getFoodId())
                .foodName(orderItem.getFoodName())
                .foodSlug(orderItem.getFoodSlug())
                .imageUrl(orderItem.getImageUrl())
                .quantity(orderItem.getQuantity())
                .price(orderItem.getPrice())
                .build();
    }
}