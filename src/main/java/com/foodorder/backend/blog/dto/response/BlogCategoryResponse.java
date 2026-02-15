package com.foodorder.backend.blog.dto.response;

import com.foodorder.backend.blog.entity.BlogType;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO response cho danh mục tin tức
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogCategoryResponse {

    private Long id;

    private String name;

    private String slug;

    private String description;

    // Loại nội dung mà danh mục này thuộc về
    private BlogType blogType;

    private Integer displayOrder;

    private Boolean isActive;

    // Đánh dấu dữ liệu được bảo vệ - chỉ SUPER_ADMIN mới có quyền sửa/xóa
    private Boolean isProtected;

    private Long blogCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
