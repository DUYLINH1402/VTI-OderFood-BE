package com.foodorder.backend.order.dto.response;

import com.foodorder.backend.order.entity.DeliveryType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long orderId;
    private Long userId;
    private DeliveryType deliveryType;
    private String paymentMethod;
    private Long districtId;
    private Long wardId;
    private String deliveryAddress;
    private String receiverName;
    private String receiverPhone;
    private String receiverEmail;
    private String status;
    private String paymentStatus;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;

    // Item response lồng trong này luôn cho gọn
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemResponse {
        private Long foodId;
        private String foodName;
        private String foodSlug;
        private String imageUrl;
        private Integer quantity;
        private BigDecimal price;
    }
}
