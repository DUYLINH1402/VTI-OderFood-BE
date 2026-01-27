package com.foodorder.backend.blog.entity;

/**
 * Enum đại diện cho trạng thái của bài viết
 */
public enum BlogStatus {
    DRAFT,      // Bản nháp - chưa công khai
    PUBLISHED,  // Đã xuất bản - công khai cho người dùng
    ARCHIVED    // Đã lưu trữ - không hiển thị nhưng vẫn giữ lại
}

