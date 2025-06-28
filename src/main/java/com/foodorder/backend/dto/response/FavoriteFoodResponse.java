package com.foodorder.backend.dto.response;

import com.foodorder.backend.entity.FavoriteFood;
import com.foodorder.backend.entity.Food;
import com.foodorder.backend.entity.FoodVariant;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteFoodResponse {
    private Long foodId;
    private String foodName;
    private String foodImageUrl;
    private String foodSlug;
    private Long variantId;
    private String variantName;
    private BigDecimal totalPrice; // gồm cả extra_price

    public static FavoriteFoodResponse fromEntity(FavoriteFood favorite) {
        Food food = favorite.getFood();
        FoodVariant variant = favorite.getVariant();

        BigDecimal basePrice = food.getPrice();
        BigDecimal extra = variant != null ? variant.getExtraPrice() : BigDecimal.ZERO;

        return FavoriteFoodResponse.builder()
                .foodId(food.getId())
                .foodName(food.getName())
                .foodImageUrl(food.getImageUrl())
                .foodSlug(food.getSlug())
                .variantId(variant != null ? variant.getId() : null)
                .variantName(variant != null ? variant.getName() : null)
                .totalPrice(basePrice.add(extra))
                .build();
    }

}

