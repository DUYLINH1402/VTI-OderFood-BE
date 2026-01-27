package com.foodorder.backend.blog.dto.request;

import com.foodorder.backend.blog.entity.BlogStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO tạo mới/cập nhật bài viết
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

    private Boolean isFeatured;

    @Size(max = 500, message = "Tags không được vượt quá 500 ký tự")
    private String tags;

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

