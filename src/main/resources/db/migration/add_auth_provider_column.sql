-- Migration: Thêm cột auth_provider vào bảng users
-- Mục đích: Phân biệt user đăng ký thông thường (LOCAL) và đăng nhập qua Google OAuth (GOOGLE)

ALTER TABLE users
ADD COLUMN IF NOT EXISTS auth_provider VARCHAR(20) DEFAULT 'LOCAL';

-- Cập nhật các user hiện tại thành LOCAL (đăng ký thông thường)
UPDATE users SET auth_provider = 'LOCAL' WHERE auth_provider IS NULL;

