package com.foodorder.backend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FavoriteRequest {
    private Long foodId;
    private Long variantId; // null nếu không có biến thể
}
