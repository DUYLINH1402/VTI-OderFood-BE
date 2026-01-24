package com.foodorder.backend.comment.entity;

/**
 * Enum định nghĩa trạng thái của bình luận
 * Dùng để quản lý và kiểm duyệt các bình luận
 */
public enum CommentStatus {
    ACTIVE,     // Bình luận đang hiển thị bình thường
    HIDDEN,     // Bình luận bị ẩn (do admin hoặc hệ thống tự động phát hiện nội dung tiêu cực)
    DELETED     // Bình luận đã bị xóa (soft delete)
}
