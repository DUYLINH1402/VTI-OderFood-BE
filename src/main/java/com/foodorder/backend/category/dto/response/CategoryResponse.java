package com.foodorder.backend.category.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "Response chứa thông tin danh mục món ăn")
public class CategoryResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID của danh mục", example = "1")
    private Long id;

    @Schema(description = "Tên danh mục", example = "Món chính")
    private String name;

    @Schema(description = "Mô tả danh mục", example = "Các món ăn chính trong thực đơn")
    private String description;

    @Schema(description = "ID của danh mục cha. Null nếu là danh mục gốc", example = "1")
    private Long parentId;

    @Schema(description = "Thứ tự hiển thị", example = "1")
    private Integer displayOrder;

    @Schema(description = "Có danh mục con hay không", example = "true")
    private boolean hasChildren;

    @Schema(description = "Slug của danh mục (dùng cho URL)", example = "mon-chinh")
    private String slug;
}
