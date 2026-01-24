package com.foodorder.backend.favorite.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request body để thêm/xóa món ăn yêu thích")
public class FavoriteRequest {

    @Schema(
        description = "ID của món ăn cần thêm vào yêu thích",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long foodId;

    @Schema(
        description = "ID của biến thể món ăn. Để null nếu không có biến thể",
        example = "2"
    )
    private Long variantId;
}
