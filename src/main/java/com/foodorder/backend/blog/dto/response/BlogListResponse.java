package com.foodorder.backend.blog.dto.response;

import com.foodorder.backend.blog.entity.BlogStatus;
import com.foodorder.backend.blog.entity.BlogType;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO response cho danh sách bài viết (không bao gồm content đầy đủ)
 * Dùng để hiển thị ở trang danh sách, tối ưu performance
 *
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
public class BlogListResponse {

    private Long id;

    private String title;

    private String slug;

    private String summary;

    private String thumbnail;

    private BlogStatus status;

    // Loại nội dung
    private BlogType blogType;

    private Integer viewCount;

    private Boolean isFeatured;

    // Đánh dấu dữ liệu được bảo vệ - chỉ SUPER_ADMIN mới có quyền sửa/xóa
    private Boolean isProtected;

    private String tags;

    // MEDIA_PRESS - hiển thị tên nguồn báo chí trong danh sách
    private String sourceName;

    // CATERING_SERVICES - hiển thị khoảng giá trong danh sách
    private String priceRange;

    private LocalDateTime publishedAt;

    // Thông tin danh mục cơ bản
    private CategoryInfo category;

    // Thông tin tác giả cơ bản
    private AuthorInfo author;

    private LocalDateTime createdAt;

    /**
     * Thông tin danh mục rút gọn
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CategoryInfo {
        private Long id;
        private String name;
        private String slug;
    }

    /**
     * Thông tin tác giả rút gọn
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthorInfo {
        private Long id;
        private String fullName;
        private String avatarUrl;
    }
}
