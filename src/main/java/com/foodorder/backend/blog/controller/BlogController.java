package com.foodorder.backend.blog.controller;

import com.foodorder.backend.blog.dto.response.BlogCategoryResponse;
import com.foodorder.backend.blog.dto.response.BlogListResponse;
import com.foodorder.backend.blog.dto.response.BlogResponse;
import com.foodorder.backend.blog.entity.BlogType;
import com.foodorder.backend.blog.service.BlogCategoryService;
import com.foodorder.backend.blog.service.BlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Controller quản lý bài viết/tin tức - API Public
 * Các API này không cần đăng nhập
 */
@RestController
@RequestMapping("/api/blogs")
@CrossOrigin("*")
@RequiredArgsConstructor
@Tag(name = "Blogs - Public", description = "API công khai cho bài viết/tin tức")
public class BlogController {

    private final BlogService blogService;
    private final BlogCategoryService blogCategoryService;

    // ==================== BLOG APIs ====================

    @Operation(summary = "Lấy danh sách bài viết công khai",
            description = "Lấy danh sách bài viết đã xuất bản, hỗ trợ phân trang")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping
    public ResponseEntity<Page<BlogListResponse>> getPublishedBlogs(
            @Parameter(description = "Thông tin phân trang")
            @PageableDefault(size = 10, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(blogService.getPublishedBlogs(pageable));
    }

    @Operation(summary = "Lấy danh sách bài viết nổi bật",
            description = "Lấy danh sách bài viết được đánh dấu là nổi bật")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/featured")
    public ResponseEntity<List<BlogListResponse>> getFeaturedBlogs(
            @Parameter(description = "Số lượng bài viết cần lấy")
            @RequestParam(defaultValue = "6") int limit) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(blogService.getFeaturedBlogs(limit));
    }

    // ==================== BLOG TYPE APIs ====================

    @Operation(summary = "Lấy danh sách bài viết theo loại nội dung",
            description = "Lấy danh sách bài viết theo loại: NEWS_PROMOTIONS, MEDIA_PRESS, CATERING_SERVICES")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/type/{blogType}")
    public ResponseEntity<Page<BlogListResponse>> getBlogsByType(
            @Parameter(description = "Loại nội dung (NEWS_PROMOTIONS, MEDIA_PRESS, CATERING_SERVICES)", required = true)
            @PathVariable BlogType blogType,
            @Parameter(description = "Thông tin phân trang")
            @PageableDefault(size = 10, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(blogService.getPublishedBlogsByType(blogType, pageable));
    }

    @Operation(summary = "Lấy danh sách bài viết nổi bật theo loại nội dung",
            description = "Lấy danh sách bài viết nổi bật theo loại: NEWS_PROMOTIONS, MEDIA_PRESS, CATERING_SERVICES")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/type/{blogType}/featured")
    public ResponseEntity<List<BlogListResponse>> getFeaturedBlogsByType(
            @Parameter(description = "Loại nội dung (NEWS_PROMOTIONS, MEDIA_PRESS, CATERING_SERVICES)", required = true)
            @PathVariable BlogType blogType,
            @Parameter(description = "Số lượng bài viết cần lấy")
            @RequestParam(defaultValue = "6") int limit) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(blogService.getFeaturedBlogsByType(blogType, limit));
    }

    @Operation(summary = "Tìm kiếm bài viết",
            description = "Tìm kiếm bài viết theo từ khóa trong tiêu đề, tóm tắt và tags")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/search")
    public ResponseEntity<Page<BlogListResponse>> searchBlogs(
            @Parameter(description = "Từ khóa tìm kiếm", required = true)
            @RequestParam String keyword,
            @Parameter(description = "Thông tin phân trang")
            @PageableDefault(size = 10, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(blogService.searchPublishedBlogs(keyword, pageable));
    }

    @Operation(summary = "Lấy chi tiết bài viết theo slug",
            description = "Lấy nội dung đầy đủ của bài viết, tự động tăng lượt xem")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/{slug}")
    public ResponseEntity<BlogResponse> getBlogBySlug(
            @Parameter(description = "Slug của bài viết", required = true)
            @PathVariable String slug) {
        return ResponseEntity.ok(blogService.getPublishedBlogBySlug(slug));
    }

    @Operation(summary = "Lấy bài viết liên quan",
            description = "Lấy danh sách bài viết cùng danh mục với bài viết hiện tại")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/{id}/related")
    public ResponseEntity<List<BlogListResponse>> getRelatedBlogs(
            @Parameter(description = "ID của bài viết hiện tại", required = true)
            @PathVariable Long id,
            @Parameter(description = "Số lượng bài viết liên quan cần lấy")
            @RequestParam(defaultValue = "4") int limit) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(blogService.getRelatedBlogs(id, limit));
    }

    // ==================== CATEGORY APIs ====================

    @Operation(summary = "Lấy danh sách danh mục blog",
            description = "Lấy danh sách danh mục đang hoạt động")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/categories")
    public ResponseEntity<List<BlogCategoryResponse>> getActiveCategories() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(blogCategoryService.getActiveCategories());
    }

    @Operation(summary = "Lấy danh sách danh mục blog theo loại nội dung",
            description = "Lấy danh sách danh mục đang hoạt động theo loại: NEWS_PROMOTIONS, MEDIA_PRESS, CATERING_SERVICES")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/categories/type/{blogType}")
    public ResponseEntity<List<BlogCategoryResponse>> getActiveCategoriesByType(
            @Parameter(description = "Loại nội dung (NEWS_PROMOTIONS, MEDIA_PRESS, CATERING_SERVICES)", required = true)
            @PathVariable BlogType blogType) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(blogCategoryService.getActiveCategoriesByType(blogType));
    }

    @Operation(summary = "Lấy chi tiết danh mục theo slug",
            description = "Lấy thông tin danh mục và số lượng bài viết")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/categories/{slug}")
    public ResponseEntity<BlogCategoryResponse> getCategoryBySlug(
            @Parameter(description = "Slug của danh mục", required = true)
            @PathVariable String slug) {
        return ResponseEntity.ok(blogCategoryService.getCategoryBySlug(slug));
    }

    @Operation(summary = "Lấy bài viết theo danh mục",
            description = "Lấy danh sách bài viết thuộc danh mục theo slug")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/categories/{slug}/posts")
    public ResponseEntity<Page<BlogListResponse>> getBlogsByCategory(
            @Parameter(description = "Slug của danh mục", required = true)
            @PathVariable String slug,
            @Parameter(description = "Thông tin phân trang")
            @PageableDefault(size = 10, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(blogService.getPublishedBlogsByCategorySlug(slug, pageable));
    }
}

