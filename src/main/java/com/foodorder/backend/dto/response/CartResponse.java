package com.foodorder.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class CartResponse {
    private Long foodId;
    private String foodName;
    private String imageUrl;
    private BigDecimal price;
    private Long variantId;
    private String variantName;
    private int quantity;
    private String slug;

}

