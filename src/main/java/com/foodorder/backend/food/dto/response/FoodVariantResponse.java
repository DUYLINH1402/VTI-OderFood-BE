package com.foodorder.backend.food.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response chứa thông tin biến thể món ăn (size, topping...)")
public class FoodVariantResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID của biến thể", example = "1")
    private Long id;

    @Schema(description = "Tên biến thể", example = "Size L")
    private String name;

    @Schema(description = "Giá phụ thu của biến thể (VND)", example = "5000")
    private BigDecimal extraPrice;

    @Schema(description = "Là biến thể mặc định hay không", example = "true")
    private boolean isDefault;
}