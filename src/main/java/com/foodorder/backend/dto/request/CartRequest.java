package com.foodorder.backend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartRequest {
    private Long foodId;
    private Long variantId;
    private int quantity;
}

