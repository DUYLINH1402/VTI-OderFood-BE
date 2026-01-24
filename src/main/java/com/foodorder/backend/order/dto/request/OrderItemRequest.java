package com.foodorder.backend.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Thông tin một món ăn trong đơn hàng")
public class OrderItemRequest {

    @Schema(description = "ID của món ăn", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long foodId;

    @Schema(description = "Số lượng", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;

    @Schema(description = "Giá của món ăn (VND). FE truyền lên, BE có thể tính lại nếu cần", example = "55000")
    private BigDecimal price;
}
