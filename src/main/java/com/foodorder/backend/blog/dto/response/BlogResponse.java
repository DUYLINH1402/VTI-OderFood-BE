package com.foodorder.backend.blog.dto.response;

import com.foodorder.backend.blog.entity.BlogStatus;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO response cho bài viết
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogResponse {

    private Long id;

    private String title;

    private String slug;

    private String summary;

    private String content;

    private String thumbnail;

    private BlogStatus status;

    private Integer viewCount;

    private Boolean isFeatured;

    // Đánh dấu dữ liệu được bảo vệ - chỉ SUPER_ADMIN mới có quyền sửa/xóa
    private Boolean isProtected;

    private String tags;

    // SEO fields
    private String metaTitle;

    private String metaDescription;

    private LocalDateTime publishedAt;

    // Thông tin danh mục
    private BlogCategoryResponse category;

    // Thông tin tác giả
    private AuthorResponse author;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * DTO thông tin tác giả (thông tin cơ bản)
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthorResponse {
        private Long id;
        private String fullName;
        private String avatarUrl;
    }
}
