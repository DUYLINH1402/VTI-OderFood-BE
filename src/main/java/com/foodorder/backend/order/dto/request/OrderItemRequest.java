package com.foodorder.backend.order.dto.request;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequest {
    private Long foodId;
    private Integer quantity;
    private BigDecimal price; // FE truyền lên, BE có thể tính lại nếu cần
}
