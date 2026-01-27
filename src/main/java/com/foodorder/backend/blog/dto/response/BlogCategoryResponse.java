package com.foodorder.backend.blog.dto.response;

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

    private Integer displayOrder;

    private Boolean isActive;

    private Long blogCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}

