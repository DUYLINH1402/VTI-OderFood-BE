package com.foodorder.backend.favorite.dto.response;

import com.foodorder.backend.favorite.entity.FavoriteFood;
import com.foodorder.backend.food.entity.Food;
import com.foodorder.backend.food.entity.FoodVariant;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response chứa thông tin món ăn yêu thích")
public class FavoriteFoodResponse {

    @Schema(description = "ID của món ăn", example = "1")
    private Long foodId;

    @Schema(description = "Tên món ăn", example = "Phở bò tái")
    private String foodName;

    @Schema(description = "URL hình ảnh món ăn", example = "https://example.com/pho.jpg")
    private String foodImageUrl;

    @Schema(description = "Slug của món ăn (dùng cho URL)", example = "pho-bo-tai")
    private String foodSlug;

    @Schema(description = "ID của biến thể. Null nếu không có biến thể", example = "2")
    private Long variantId;

    @Schema(description = "Tên biến thể. Null nếu không có biến thể", example = "Size L")
    private String variantName;

    @Schema(description = "Tổng giá (bao gồm cả phụ thu biến thể)", example = "60000")
    private BigDecimal totalPrice;

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

