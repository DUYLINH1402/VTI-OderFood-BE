-- ===========================================
-- Initial Database Setup Script
-- Script này sẽ chạy tự động khi khởi tạo MySQL container lần đầu
-- ===========================================

-- Đảm bảo database tồn tại
CREATE DATABASE IF NOT EXISTS food_ordering_system
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE food_ordering_system;

-- Thiết lập timezone
SET GLOBAL time_zone = '+07:00';

-- Hiển thị thông báo
SELECT 'Database food_ordering_system đã được khởi tạo thành công!' AS message;

