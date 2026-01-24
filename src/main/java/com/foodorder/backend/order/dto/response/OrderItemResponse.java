package com.foodorder.backend.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response chứa thông tin một món ăn trong đơn hàng")
public class OrderItemResponse {

    @Schema(description = "ID của món ăn", example = "1")
    private Long foodId;

    @Schema(description = "Tên món ăn", example = "Phở bò tái")
    private String foodName;

    @Schema(description = "Slug của món ăn (dùng cho URL)", example = "pho-bo-tai")
    private String foodSlug;

    @Schema(description = "Số lượng", example = "2")
    private Integer quantity;

    @Schema(description = "Đơn giá (VND)", example = "50000")
    private BigDecimal price;

    @Schema(description = "Đơn giá đã format", example = "55.000đ")
    private String priceFormatted;

    @Schema(description = "Thành tiền đã format", example = "110.000đ")
    private String totalFormatted;
}
