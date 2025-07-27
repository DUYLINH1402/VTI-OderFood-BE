package com.foodorder.backend.order.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {
    private Long foodId;
    private String foodName;
    private String foodSlug;
    private Integer quantity;
    private BigDecimal price;
    private String priceFormatted;
    private String totalFormatted;
}
