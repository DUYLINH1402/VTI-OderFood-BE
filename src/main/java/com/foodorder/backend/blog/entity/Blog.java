package com.foodorder.backend.blog.entity;

import com.foodorder.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho bài viết/tin tức
 * Lưu trữ toàn bộ nội dung bài viết với hỗ trợ SEO và lên lịch đăng bài
 */
@Entity
@Table(name = "blogs", indexes = {
        @Index(name = "idx_blogs_slug", columnList = "slug"),
        @Index(name = "idx_blogs_status", columnList = "status"),
        @Index(name = "idx_blogs_category", columnList = "category_id"),
        @Index(name = "idx_blogs_published_at", columnList = "published_at"),
        @Index(name = "idx_blogs_is_featured", columnList = "is_featured")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Blog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "slug", nullable = false, unique = true, length = 300)
    private String slug;

    @Column(name = "summary", length = 500)
    private String summary;

    // Nội dung chi tiết bài viết (hỗ trợ HTML/JSON từ editor)
    @Lob
    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "thumbnail", length = 500)
    private String thumbnail;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private BlogStatus status = BlogStatus.DRAFT;

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    // Đánh dấu bài viết nổi bật
    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    // Đánh dấu dữ liệu được bảo vệ - chỉ SUPER_ADMIN mới có quyền sửa/xóa
    @Column(name = "is_protected")
    @Builder.Default
    private Boolean isProtected = false;

    // Tags để phân loại linh hoạt (lưu dạng comma-separated, ví dụ: "khuyến mãi,ưu đãi,tháng 1")
    @Column(name = "tags", length = 500)
    private String tags;

    // SEO fields
    @Column(name = "meta_title", length = 255)
    private String metaTitle;

    @Column(name = "meta_description", length = 500)
    private String metaDescription;

    // Thời điểm xuất bản (hỗ trợ lên lịch đăng bài)
    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    // Người viết bài (Admin hoặc Staff)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User author;

    // Danh mục bài viết
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private BlogCategory category;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Kiểm tra bài viết có đang được công khai không
     * (đã xuất bản và thời điểm xuất bản đã đến)
     */
    public boolean isPubliclyVisible() {
        if (status != BlogStatus.PUBLISHED) {
            return false;
        }
        if (publishedAt == null) {
            return true;
        }
        return !publishedAt.isAfter(LocalDateTime.now());
    }
}
