package com.foodorder.backend.blog.dto.request;

import com.foodorder.backend.blog.entity.BlogStatus;
import com.foodorder.backend.blog.entity.BlogType;
import lombok.*;

/**
 * DTO lọc danh sách bài viết (Admin)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogFilterRequest {

    private String title;

    private BlogStatus status;

    // Lọc theo loại nội dung
    private BlogType blogType;

    private Long categoryId;

    private Long authorId;
}

