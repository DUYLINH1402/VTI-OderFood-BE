package com.foodorder.backend.cart.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request body để thêm/cập nhật món ăn trong giỏ hàng")
public class CartRequest {

    @Schema(
        description = "ID của món ăn cần thêm vào giỏ hàng",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private Long foodId;

    @Schema(
        description = "ID của biến thể món ăn (size, topping...). Để null nếu món không có biến thể",
        example = "2",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED
    )
    private Long variantId;

    @Schema(
        description = "Số lượng món ăn cần thêm. Số âm để giảm, số dương để tăng, 0 để xóa khỏi giỏ",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private int quantity;
}

