package com.foodorder.backend.blog.service;

import com.foodorder.backend.blog.dto.request.BlogCategoryRequest;
import com.foodorder.backend.blog.dto.response.BlogCategoryResponse;

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
     * Lấy chi tiết danh mục theo slug (Public)
     */
    BlogCategoryResponse getCategoryBySlug(String slug);

    // ==================== ADMIN APIs ====================

    /**
     * Lấy tất cả danh mục (Admin)
     */
    List<BlogCategoryResponse> getAllCategories();

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

