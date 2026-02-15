package com.foodorder.backend.blog.dto.response;

import com.foodorder.backend.blog.entity.BlogStatus;
import com.foodorder.backend.blog.entity.BlogType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO response cho bài viết
 * Hỗ trợ 3 loại nội dung:
 * - NEWS_PROMOTIONS: Tin tức, khuyến mãi
 * - MEDIA_PRESS: Báo chí nói về nhà hàng
 * - CATERING_SERVICES: Dịch vụ đãi tiệc
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

    // Loại nội dung
    private BlogType blogType;

    private Integer viewCount;

    private Boolean isFeatured;

    // Đánh dấu dữ liệu được bảo vệ - chỉ SUPER_ADMIN mới có quyền sửa/xóa
    private Boolean isProtected;

    private String tags;

    // ========== MEDIA_PRESS fields ==========
    private String sourceUrl;
    private String sourceName;
    private String sourceLogo;
    private LocalDateTime sourcePublishedAt;

    // ========== CATERING_SERVICES fields ==========
    private String priceRange;
    private String serviceAreas;
    private String menuItems;
    private List<String> galleryImages;
    private Integer minCapacity;
    private Integer maxCapacity;
    private String contactInfo;

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
