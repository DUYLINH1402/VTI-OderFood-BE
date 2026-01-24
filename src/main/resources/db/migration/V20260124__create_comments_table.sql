-- Migration: Tạo bảng comments cho hệ thống bình luận dùng chung (Polymorphic)
-- Version: V20260124
-- Description: Hỗ trợ bình luận cho nhiều loại đối tượng (Food, Blog, Movie...)
--              với tính năng reply (bình luận phân cấp 2 level)

-- Tạo bảng comments
CREATE TABLE IF NOT EXISTS comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    target_type VARCHAR(20) NOT NULL COMMENT 'Loại đối tượng: FOOD, BLOG, MOVIE...',
    target_id BIGINT NOT NULL COMMENT 'ID của đối tượng được bình luận',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'Trạng thái: ACTIVE, HIDDEN, DELETED',
    parent_id BIGINT DEFAULT NULL COMMENT 'ID của comment cha (null nếu là comment gốc)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign keys
    CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_comments_parent FOREIGN KEY (parent_id) REFERENCES comments(id) ON DELETE CASCADE,

    -- Indexes cho tối ưu truy vấn
    INDEX idx_comments_target (target_type, target_id),
    INDEX idx_comments_user (user_id),
    INDEX idx_comments_parent (parent_id),
    INDEX idx_comments_status (status),
    INDEX idx_comments_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng lưu trữ bình luận dùng chung cho nhiều loại đối tượng';

-- Thêm cột total_comments vào bảng foods (tùy chọn, để đếm nhanh số comment)
-- ALTER TABLE foods ADD COLUMN total_comments INT DEFAULT 0 COMMENT 'Tổng số bình luận';

