package com.foodorder.backend.blog.dto.request;

import com.foodorder.backend.blog.entity.BlogType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO tạo mới/cập nhật danh mục tin tức
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogCategoryRequest {

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(max = 100, message = "Tên danh mục không được vượt quá 100 ký tự")
    private String name;

    @Size(max = 150, message = "Slug không được vượt quá 150 ký tự")
    private String slug;

    @Size(max = 500, message = "Mô tả không được vượt quá 500 ký tự")
    private String description;

    // Loại nội dung mà danh mục này thuộc về (mặc định NEWS_PROMOTIONS nếu không truyền)
    private BlogType blogType;

    private Integer displayOrder;

    private Boolean isActive;
}

