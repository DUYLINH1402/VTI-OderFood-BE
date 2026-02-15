package com.foodorder.backend.blog.repository;

import com.foodorder.backend.blog.entity.BlogCategory;
import com.foodorder.backend.blog.entity.BlogType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý danh mục tin tức
 */
@Repository
public interface BlogCategoryRepository extends JpaRepository<BlogCategory, Long> {

    /**
     * Tìm danh mục theo slug
     */
    Optional<BlogCategory> findBySlug(String slug);

    /**
     * Kiểm tra slug đã tồn tại chưa
     */
    boolean existsBySlug(String slug);

    /**
     * Kiểm tra slug đã tồn tại (ngoại trừ danh mục hiện tại - dùng khi update)
     */
    boolean existsBySlugAndIdNot(String slug, Long id);

    /**
     * Lấy danh sách danh mục đang hoạt động, sắp xếp theo thứ tự hiển thị
     */
    List<BlogCategory> findByIsActiveTrueOrderByDisplayOrderAsc();

    /**
     * Lấy danh sách danh mục đang hoạt động theo loại nội dung
     */
    List<BlogCategory> findByIsActiveTrueAndBlogTypeOrderByDisplayOrderAsc(BlogType blogType);

    /**
     * Lấy tất cả danh mục sắp xếp theo thứ tự hiển thị
     */
    List<BlogCategory> findAllByOrderByDisplayOrderAsc();

    /**
     * Lấy tất cả danh mục theo loại nội dung
     */
    List<BlogCategory> findByBlogTypeOrderByDisplayOrderAsc(BlogType blogType);
}

