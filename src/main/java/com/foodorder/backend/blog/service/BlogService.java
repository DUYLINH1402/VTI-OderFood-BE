package com.foodorder.backend.blog.service;

import com.foodorder.backend.blog.dto.request.BlogFilterRequest;
import com.foodorder.backend.blog.dto.request.BlogRequest;
import com.foodorder.backend.blog.dto.response.BlogListResponse;
import com.foodorder.backend.blog.dto.response.BlogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service interface quản lý bài viết/tin tức
 */
public interface BlogService {

    // ==================== PUBLIC APIs ====================

    /**
     * Lấy danh sách bài viết công khai (đã xuất bản)
     */
    Page<BlogListResponse> getPublishedBlogs(Pageable pageable);

    /**
     * Lấy danh sách bài viết công khai theo danh mục
     */
    Page<BlogListResponse> getPublishedBlogsByCategory(Long categoryId, Pageable pageable);

    /**
     * Lấy danh sách bài viết công khai theo slug danh mục
     */
    Page<BlogListResponse> getPublishedBlogsByCategorySlug(String categorySlug, Pageable pageable);

    /**
     * Lấy danh sách bài viết nổi bật
     */
    List<BlogListResponse> getFeaturedBlogs(int limit);

    /**
     * Tìm kiếm bài viết công khai theo từ khóa
     */
    Page<BlogListResponse> searchPublishedBlogs(String keyword, Pageable pageable);

    /**
     * Lấy chi tiết bài viết công khai theo slug
     * Tự động tăng lượt xem
     */
    BlogResponse getPublishedBlogBySlug(String slug);

    /**
     * Lấy danh sách bài viết liên quan
     */
    List<BlogListResponse> getRelatedBlogs(Long blogId, int limit);

    // ==================== ADMIN APIs ====================

    /**
     * Lấy danh sách bài viết với bộ lọc (Admin)
     */
    Page<BlogListResponse> getBlogsWithFilter(BlogFilterRequest filterRequest, Pageable pageable);

    /**
     * Lấy chi tiết bài viết theo ID (Admin)
     */
    BlogResponse getBlogById(Long id);

    /**
     * Tạo mới bài viết (Admin/Staff)
     */
    BlogResponse createBlog(BlogRequest request, Long authorId);

    /**
     * Cập nhật bài viết (Admin/Staff)
     */
    BlogResponse updateBlog(Long id, BlogRequest request);

    /**
     * Xóa bài viết (Admin)
     */
    void deleteBlog(Long id);

    /**
     * Thay đổi trạng thái bài viết (Admin/Staff)
     */
    BlogResponse updateBlogStatus(Long id, String status);
}

