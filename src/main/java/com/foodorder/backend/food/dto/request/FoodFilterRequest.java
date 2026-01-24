package com.foodorder.backend.food.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * DTO dùng để lọc danh sách món ăn trong trang quản lý
 * Hỗ trợ lọc theo tên, trạng thái, danh mục, và trạng thái hoạt động
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request params để lọc danh sách món ăn")
public class FoodFilterRequest {

    @Schema(description = "Lọc theo tên món ăn (tìm kiếm gần đúng)", example = "phở")
    private String name;

    @Schema(description = "Lọc theo trạng thái món ăn", example = "AVAILABLE", allowableValues = {"AVAILABLE", "UNAVAILABLE"})
    private String status;

    @Schema(description = "Lọc theo ID danh mục", example = "1")
    private Long categoryId;

    @Schema(description = "Lọc theo trạng thái hoạt động", example = "true")
    private Boolean isActive;
}

