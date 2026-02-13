-- Migration: Tạo bảng contact_messages để lưu tin nhắn liên hệ từ khách hàng
-- Version: V20260212
-- Author: System
-- Date: 2026-02-12

CREATE TABLE IF NOT EXISTS contact_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Thông tin người gửi
    name VARCHAR(100) NOT NULL COMMENT 'Tên người gửi',
    email VARCHAR(255) NOT NULL COMMENT 'Email người gửi',
    phone VARCHAR(20) NULL COMMENT 'Số điện thoại (tùy chọn)',
    subject VARCHAR(200) NULL COMMENT 'Chủ đề tin nhắn (tùy chọn)',
    message TEXT NOT NULL COMMENT 'Nội dung tin nhắn',

    -- Trạng thái và quản lý
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'Trạng thái: PENDING, READ, REPLIED, ARCHIVED',
    ip_address VARCHAR(45) NULL COMMENT 'IP của người gửi (dùng cho rate limiting)',
    admin_note TEXT NULL COMMENT 'Ghi chú nội bộ của admin',

    -- Phản hồi
    reply_content TEXT NULL COMMENT 'Nội dung phản hồi từ admin',
    replied_at DATETIME NULL COMMENT 'Thời gian phản hồi',
    replied_by BIGINT NULL COMMENT 'ID admin đã phản hồi',

    -- Trạng thái thông báo
    notification_sent BOOLEAN DEFAULT FALSE COMMENT 'Đã gửi thông báo cho admin chưa',

    -- Timestamps
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian tạo',
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP COMMENT 'Thời gian cập nhật',

    -- Indexes để tối ưu truy vấn
    INDEX idx_contact_email (email),
    INDEX idx_contact_status (status),
    INDEX idx_contact_created_at (created_at),
    INDEX idx_contact_ip_created (ip_address, created_at),
    INDEX idx_contact_email_created (email, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng lưu trữ tin nhắn liên hệ từ khách hàng';

-- Thêm foreign key cho replied_by nếu bảng users tồn tại
-- ALTER TABLE contact_messages ADD CONSTRAINT fk_contact_replied_by FOREIGN KEY (replied_by) REFERENCES users(id) ON DELETE SET NULL;

