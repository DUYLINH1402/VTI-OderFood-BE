package com.foodorder.backend.cart.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "Response chứa thông tin chi tiết một món trong giỏ hàng")
public class CartResponse {

    @Schema(description = "ID của món ăn", example = "1")
    private Long foodId;

    @Schema(description = "Tên món ăn", example = "Phở bò tái")
    private String foodName;

    @Schema(description = "URL hình ảnh món ăn", example = "https://example.com/images/pho.jpg")
    private String imageUrl;

    @Schema(description = "Giá của món ăn (đã bao gồm biến thể nếu có)", example = "55000")
    private BigDecimal price;

    @Schema(description = "ID của biến thể món ăn. Null nếu không có biến thể", example = "2")
    private Long variantId;

    @Schema(description = "Tên biến thể (size, topping...). Null nếu không có biến thể", example = "Size L")
    private String variantName;

    @Schema(description = "Số lượng món trong giỏ hàng", example = "2")
    private int quantity;

    @Schema(description = "Slug của món ăn (dùng cho URL)", example = "pho-bo-tai")
    private String slug;

}

