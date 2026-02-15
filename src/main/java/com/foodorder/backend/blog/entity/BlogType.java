package com.foodorder.backend.blog.entity;

/**
 * Enum phân loại nội dung bài viết theo chiến lược content
 * - NEWS_PROMOTIONS: Tin tức nội bộ, khuyến mãi (Blog truyền thống)
 * - MEDIA_PRESS: Báo chí nói về nhà hàng (có thêm link gốc bài báo)
 * - CATERING_SERVICES: Dịch vụ đãi tiệc lưu động (showcase gói tiệc, thực đơn, hình ảnh)
 */
public enum BlogType {
    NEWS_PROMOTIONS,    // Tin tức, khuyến mãi
    MEDIA_PRESS,        // Báo chí, truyền thông
    CATERING_SERVICES   // Dịch vụ đãi tiệc
}

