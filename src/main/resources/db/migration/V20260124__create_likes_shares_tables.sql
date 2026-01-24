-- Migration: Tạo bảng likes và shares, thêm cột total_likes, total_shares vào bảng foods
-- Ngày tạo: 2026-01-24

-- Thêm cột total_likes và total_shares vào bảng foods (nếu chưa có)
ALTER TABLE foods ADD COLUMN IF NOT EXISTS total_likes INT DEFAULT 0;
ALTER TABLE foods ADD COLUMN IF NOT EXISTS total_shares INT DEFAULT 0;

-- Tạo bảng likes để lưu lượt thích
CREATE TABLE IF NOT EXISTS likes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    target_type VARCHAR(20) NOT NULL COMMENT 'Loại đối tượng: FOOD, BLOG, MOVIE',
    target_id BIGINT NOT NULL COMMENT 'ID của đối tượng được like',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key tới bảng users
    CONSTRAINT fk_likes_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    -- Unique constraint: Một user chỉ like một đối tượng một lần
    CONSTRAINT uk_likes_user_target UNIQUE (user_id, target_type, target_id),

    -- Index để tối ưu truy vấn đếm like theo target
    INDEX idx_likes_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tạo bảng shares để lưu vết lượt chia sẻ
CREATE TABLE IF NOT EXISTS shares (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL COMMENT 'Có thể NULL nếu khách vãng lai share',
    target_type VARCHAR(20) NOT NULL COMMENT 'Loại đối tượng: FOOD, BLOG',
    target_id BIGINT NOT NULL COMMENT 'ID của đối tượng được share',
    platform VARCHAR(20) NOT NULL COMMENT 'Nền tảng: FACEBOOK, ZALO',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key tới bảng users (cho phép NULL)
    CONSTRAINT fk_shares_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,

    -- Index để tối ưu truy vấn đếm share theo target
    INDEX idx_shares_target (target_type, target_id),
    INDEX idx_shares_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Cập nhật giá trị total_likes cho các món ăn đã có (nếu có dữ liệu cũ)
-- UPDATE foods f SET f.total_likes = (SELECT COUNT(*) FROM likes l WHERE l.target_type = 'FOOD' AND l.target_id = f.id);
-- UPDATE foods f SET f.total_shares = (SELECT COUNT(*) FROM shares s WHERE s.target_type = 'FOOD' AND s.target_id = f.id);

