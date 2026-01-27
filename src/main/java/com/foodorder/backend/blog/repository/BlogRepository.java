package com.foodorder.backend.blog.repository;

import com.foodorder.backend.blog.entity.Blog;
import com.foodorder.backend.blog.entity.BlogStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý bài viết/tin tức
 */
@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {

    /**
     * Tìm bài viết theo slug
     */
    Optional<Blog> findBySlug(String slug);

    /**
     * Kiểm tra slug đã tồn tại chưa
     */
    boolean existsBySlug(String slug);

    /**
     * Kiểm tra slug đã tồn tại (ngoại trừ bài viết hiện tại - dùng khi update)
     */
    boolean existsBySlugAndIdNot(String slug, Long id);

    /**
     * Lấy danh sách bài viết công khai (đã xuất bản và thời điểm xuất bản đã đến)
     */
    @Query(value = "SELECT b FROM Blog b LEFT JOIN FETCH b.category LEFT JOIN FETCH b.author " +
            "WHERE b.status = 'PUBLISHED' " +
            "AND (b.publishedAt IS NULL OR b.publishedAt <= :now)",
            countQuery = "SELECT COUNT(b) FROM Blog b WHERE b.status = 'PUBLISHED' " +
                    "AND (b.publishedAt IS NULL OR b.publishedAt <= :now)")
    Page<Blog> findPublishedBlogs(@Param("now") LocalDateTime now, Pageable pageable);

    /**
     * Lấy danh sách bài viết công khai theo danh mục
     */
    @Query(value = "SELECT b FROM Blog b LEFT JOIN FETCH b.category LEFT JOIN FETCH b.author " +
            "WHERE b.status = 'PUBLISHED' " +
            "AND (b.publishedAt IS NULL OR b.publishedAt <= :now) " +
            "AND b.category.id = :categoryId",
            countQuery = "SELECT COUNT(b) FROM Blog b WHERE b.status = 'PUBLISHED' " +
                    "AND (b.publishedAt IS NULL OR b.publishedAt <= :now) " +
                    "AND b.category.id = :categoryId")
    Page<Blog> findPublishedBlogsByCategory(
            @Param("categoryId") Long categoryId,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    /**
     * Lấy danh sách bài viết công khai theo slug danh mục
     */
    @Query(value = "SELECT b FROM Blog b LEFT JOIN FETCH b.category LEFT JOIN FETCH b.author " +
            "WHERE b.status = 'PUBLISHED' " +
            "AND (b.publishedAt IS NULL OR b.publishedAt <= :now) " +
            "AND b.category.slug = :categorySlug",
            countQuery = "SELECT COUNT(b) FROM Blog b WHERE b.status = 'PUBLISHED' " +
                    "AND (b.publishedAt IS NULL OR b.publishedAt <= :now) " +
                    "AND b.category.slug = :categorySlug")
    Page<Blog> findPublishedBlogsByCategorySlug(
            @Param("categorySlug") String categorySlug,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    /**
     * Lấy danh sách bài viết nổi bật công khai
     */
    @Query("SELECT b FROM Blog b LEFT JOIN FETCH b.category LEFT JOIN FETCH b.author " +
            "WHERE b.status = 'PUBLISHED' " +
            "AND b.isFeatured = true " +
            "AND (b.publishedAt IS NULL OR b.publishedAt <= :now)")
    List<Blog> findFeaturedBlogs(@Param("now") LocalDateTime now, Pageable pageable);

    /**
     * Tìm kiếm bài viết công khai theo từ khóa (tiêu đề, tóm tắt, tags)
     */
    @Query(value = "SELECT b FROM Blog b LEFT JOIN FETCH b.category LEFT JOIN FETCH b.author " +
            "WHERE b.status = 'PUBLISHED' " +
            "AND (b.publishedAt IS NULL OR b.publishedAt <= :now) " +
            "AND (LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.summary) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(b.tags) LIKE LOWER(CONCAT('%', :keyword, '%')))",
            countQuery = "SELECT COUNT(b) FROM Blog b WHERE b.status = 'PUBLISHED' " +
                    "AND (b.publishedAt IS NULL OR b.publishedAt <= :now) " +
                    "AND (LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "OR LOWER(b.summary) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
                    "OR LOWER(b.tags) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Blog> searchPublishedBlogs(
            @Param("keyword") String keyword,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    /**
     * Lấy bài viết công khai theo slug (dùng cho trang chi tiết)
     */
    @Query("SELECT b FROM Blog b LEFT JOIN FETCH b.category LEFT JOIN FETCH b.author " +
            "WHERE b.slug = :slug " +
            "AND b.status = 'PUBLISHED' " +
            "AND (b.publishedAt IS NULL OR b.publishedAt <= :now)")
    Optional<Blog> findPublishedBlogBySlug(@Param("slug") String slug, @Param("now") LocalDateTime now);

    /**
     * Tăng lượt xem bài viết
     */
    @Modifying
    @Transactional
    @Query("UPDATE Blog b SET b.viewCount = b.viewCount + 1 WHERE b.id = :id")
    void incrementViewCount(@Param("id") Long id);

    // ==================== ADMIN APIs ====================

    /**
     * Lấy danh sách tất cả bài viết với bộ lọc (Admin)
     */
    @Query(value = "SELECT b FROM Blog b LEFT JOIN FETCH b.category LEFT JOIN FETCH b.author " +
            "WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:status IS NULL OR b.status = :status) " +
            "AND (:categoryId IS NULL OR b.category.id = :categoryId) " +
            "AND (:authorId IS NULL OR b.author.id = :authorId)",
            countQuery = "SELECT COUNT(b) FROM Blog b " +
                    "WHERE (:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
                    "AND (:status IS NULL OR b.status = :status) " +
                    "AND (:categoryId IS NULL OR b.category.id = :categoryId) " +
                    "AND (:authorId IS NULL OR b.author.id = :authorId)")
    Page<Blog> findWithFilter(
            @Param("title") String title,
            @Param("status") BlogStatus status,
            @Param("categoryId") Long categoryId,
            @Param("authorId") Long authorId,
            Pageable pageable);

    /**
     * Đếm số bài viết theo trạng thái
     */
    long countByStatus(BlogStatus status);

    /**
     * Đếm số bài viết theo danh mục
     */
    long countByCategoryId(Long categoryId);

    /**
     * Lấy bài viết liên quan (cùng danh mục, loại trừ bài hiện tại)
     */
    @Query("SELECT b FROM Blog b LEFT JOIN FETCH b.category " +
            "WHERE b.status = 'PUBLISHED' " +
            "AND (b.publishedAt IS NULL OR b.publishedAt <= :now) " +
            "AND b.category.id = :categoryId " +
            "AND b.id <> :excludeId")
    List<Blog> findRelatedBlogs(
            @Param("categoryId") Long categoryId,
            @Param("excludeId") Long excludeId,
            @Param("now") LocalDateTime now,
            Pageable pageable);
}

