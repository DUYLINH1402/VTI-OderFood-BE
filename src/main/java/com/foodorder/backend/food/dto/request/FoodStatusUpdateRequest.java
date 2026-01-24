package com.foodorder.backend.food.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * DTO dùng để thay đổi trạng thái món ăn
 * Staff có thể thay đổi status (AVAILABLE/UNAVAILABLE) hoặc isActive
 * Có thể thêm ghi chú lý do hết hàng
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body để cập nhật trạng thái món ăn")
public class FoodStatusUpdateRequest {

    @Schema(
        description = "Trạng thái món ăn",
        example = "AVAILABLE",
        allowableValues = {"AVAILABLE", "UNAVAILABLE"}
    )
    private String status;

    @Schema(description = "Trạng thái hoạt động của món ăn", example = "true")
    private Boolean isActive;

    @Schema(
        description = "Ghi chú lý do thay đổi trạng thái (VD: hết nguyên liệu, bảo trì...)",
        example = "Tạm hết nguyên liệu, dự kiến có lại vào ngày mai"
    )
    private String statusNote;
}
