package com.foodorder.backend.dto.response;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodVariantResponse {
    private Long id;
    private String name;
    private BigDecimal extraPrice;
    private boolean isDefault;
}