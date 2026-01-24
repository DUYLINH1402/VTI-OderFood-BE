package com.foodorder.backend.category.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Request body để tạo/cập nhật danh mục món ăn")
public class CategoryRequest {

    @Schema(description = "Tên danh mục", example = "Món chính", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Mô tả danh mục", example = "Các món ăn chính trong thực đơn")
    private String description;

    @Schema(description = "ID của danh mục cha (để null nếu là danh mục gốc)", example = "1")
    private Long parentId;

    @Schema(description = "Thứ tự hiển thị (số nhỏ hiển thị trước)", example = "1")
    private Integer displayOrder;

    @Schema(description = "Slug của danh mục (dùng cho URL)", example = "mon-chinh")
    private String slug;

}
