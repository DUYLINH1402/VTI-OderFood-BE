package com.foodorder.backend.blog.dto.request;

import com.foodorder.backend.blog.entity.BlogStatus;
import com.foodorder.backend.blog.entity.BlogType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO tạo mới/cập nhật bài viết
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
public class BlogRequest {

    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề không được vượt quá 255 ký tự")
    private String title;

    @Size(max = 300, message = "Slug không được vượt quá 300 ký tự")
    private String slug;

    @Size(max = 500, message = "Tóm tắt không được vượt quá 500 ký tự")
    private String summary;

    private String content;

    @Size(max = 500, message = "URL thumbnail không được vượt quá 500 ký tự")
    private String thumbnail;

    private BlogStatus status;

    // Loại nội dung (mặc định NEWS_PROMOTIONS nếu không truyền)
    private BlogType blogType;

    private Boolean isFeatured;

    @Size(max = 500, message = "Tags không được vượt quá 500 ký tự")
    private String tags;

    // ========== MEDIA_PRESS fields ==========
    @Size(max = 500, message = "URL nguồn không được vượt quá 500 ký tự")
    private String sourceUrl;

    @Size(max = 200, message = "Tên nguồn không được vượt quá 200 ký tự")
    private String sourceName;

    @Size(max = 500, message = "URL logo nguồn không được vượt quá 500 ký tự")
    private String sourceLogo;

    private LocalDateTime sourcePublishedAt;

    // ========== CATERING_SERVICES fields ==========
    @Size(max = 200, message = "Khoảng giá không được vượt quá 200 ký tự")
    private String priceRange;

    @Size(max = 1000, message = "Khu vực phục vụ không được vượt quá 1000 ký tự")
    private String serviceAreas;

    // Danh sách món ăn trong gói tiệc (JSON string hoặc list)
    private String menuItems;

    // Gallery hình ảnh thực tế (JSON array các URL hoặc list)
    private List<String> galleryImages;

    private Integer minCapacity;

    private Integer maxCapacity;

    @Size(max = 500, message = "Thông tin liên hệ không được vượt quá 500 ký tự")
    private String contactInfo;

    // SEO fields
    @Size(max = 255, message = "Meta title không được vượt quá 255 ký tự")
    private String metaTitle;

    @Size(max = 500, message = "Meta description không được vượt quá 500 ký tự")
    private String metaDescription;

    // Thời điểm xuất bản (null = xuất bản ngay khi status = PUBLISHED)
    private LocalDateTime publishedAt;

    // ID danh mục
    private Long categoryId;
}

