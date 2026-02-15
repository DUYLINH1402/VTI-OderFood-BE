package com.foodorder.backend.blog.entity;

import com.foodorder.backend.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity đại diện cho bài viết/tin tức
 * Lưu trữ toàn bộ nội dung bài viết với hỗ trợ SEO và lên lịch đăng bài
 *
 * Hỗ trợ 3 loại nội dung (BlogType):
 * - NEWS_PROMOTIONS: Tin tức nội bộ, khuyến mãi
 * - MEDIA_PRESS: Báo chí nói về nhà hàng (có sourceUrl, sourceName)
 * - CATERING_SERVICES: Dịch vụ đãi tiệc (có priceRange, serviceAreas, menuItems, galleryImages)
 */
@Entity
@Table(name = "blogs", indexes = {
        @Index(name = "idx_blogs_slug", columnList = "slug"),
        @Index(name = "idx_blogs_status", columnList = "status"),
        @Index(name = "idx_blogs_category", columnList = "category_id"),
        @Index(name = "idx_blogs_published_at", columnList = "published_at"),
        @Index(name = "idx_blogs_is_featured", columnList = "is_featured"),
        @Index(name = "idx_blogs_blog_type", columnList = "blog_type")
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

    // Phân loại nội dung: NEWS_PROMOTIONS, MEDIA_PRESS, CATERING_SERVICES
    @Enumerated(EnumType.STRING)
    @Column(name = "blog_type", nullable = false, length = 30)
    @Builder.Default
    private BlogType blogType = BlogType.NEWS_PROMOTIONS;

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

    // ========== MEDIA_PRESS fields ==========
    // Link bài báo gốc (dùng cho loại MEDIA_PRESS)
    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    // Tên nguồn báo chí (ví dụ: VnExpress, Tuổi Trẻ, Dân Trí...)
    @Column(name = "source_name", length = 200)
    private String sourceName;

    // Logo nguồn báo chí
    @Column(name = "source_logo", length = 500)
    private String sourceLogo;

    // Ngày đăng bài trên báo gốc
    @Column(name = "source_published_at")
    private LocalDateTime sourcePublishedAt;

    // ========== CATERING_SERVICES fields ==========
    // Khoảng giá dịch vụ (ví dụ: "500.000 - 2.000.000 VNĐ/người")
    @Column(name = "price_range", length = 200)
    private String priceRange;

    // Khu vực phục vụ (lưu dạng JSON hoặc comma-separated, ví dụ: "Quận 1, Quận 2, Quận 3")
    @Column(name = "service_areas", length = 1000)
    private String serviceAreas;

    // Danh sách món ăn trong gói tiệc (lưu dạng JSON)
    @Lob
    @Column(name = "menu_items", columnDefinition = "TEXT")
    private String menuItems;

    // Gallery hình ảnh thực tế (lưu dạng JSON array các URL)
    @Lob
    @Column(name = "gallery_images", columnDefinition = "TEXT")
    private String galleryImages;

    // Sức chứa tối thiểu (số người)
    @Column(name = "min_capacity")
    private Integer minCapacity;

    // Sức chứa tối đa (số người)
    @Column(name = "max_capacity")
    private Integer maxCapacity;

    // Thông tin liên hệ đặt tiệc (số điện thoại hoặc email riêng)
    @Column(name = "contact_info", length = 500)
    private String contactInfo;

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
