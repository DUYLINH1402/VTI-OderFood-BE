package com.foodorder.backend.blog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity đại diện cho danh mục tin tức/bài viết
 * Giúp phân loại tin tức (ví dụ: Khuyến mãi, Review món ăn, Tin nội bộ)
 */
@Entity
@Table(name = "blog_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 150)
    private String slug;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // Đánh dấu dữ liệu được bảo vệ - chỉ SUPER_ADMIN mới có quyền sửa/xóa
    @Column(name = "is_protected")
    @Builder.Default
    private Boolean isProtected = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Một danh mục có thể có nhiều bài viết
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Blog> blogs;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

