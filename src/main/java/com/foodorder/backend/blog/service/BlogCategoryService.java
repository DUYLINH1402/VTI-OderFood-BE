package com.foodorder.backend.blog.service;

import com.foodorder.backend.blog.dto.request.BlogCategoryRequest;
import com.foodorder.backend.blog.dto.response.BlogCategoryResponse;
import com.foodorder.backend.blog.entity.BlogType;

import java.util.List;

/**
 * Service interface quản lý danh mục tin tức
 */
public interface BlogCategoryService {

    // ==================== PUBLIC APIs ====================

    /**
     * Lấy danh sách danh mục đang hoạt động (Public)
     */
    List<BlogCategoryResponse> getActiveCategories();

    /**
     * Lấy danh sách danh mục đang hoạt động theo loại nội dung (Public)
     */
    List<BlogCategoryResponse> getActiveCategoriesByType(BlogType blogType);

    /**
     * Lấy chi tiết danh mục theo slug (Public)
     */
    BlogCategoryResponse getCategoryBySlug(String slug);

    // ==================== ADMIN APIs ====================

    /**
     * Lấy tất cả danh mục (Admin)
     */
    List<BlogCategoryResponse> getAllCategories();

    /**
     * Lấy tất cả danh mục theo loại nội dung (Admin)
     */
    List<BlogCategoryResponse> getAllCategoriesByType(BlogType blogType);

    /**
     * Lấy chi tiết danh mục theo ID (Admin)
     */
    BlogCategoryResponse getCategoryById(Long id);

    /**
     * Tạo mới danh mục (Admin)
     */
    BlogCategoryResponse createCategory(BlogCategoryRequest request);

    /**
     * Cập nhật danh mục (Admin)
     */
    BlogCategoryResponse updateCategory(Long id, BlogCategoryRequest request);

    /**
     * Xóa danh mục (Admin)
     * Chỉ cho phép xóa khi không có bài viết nào thuộc danh mục
     */
    void deleteCategory(Long id);
}

