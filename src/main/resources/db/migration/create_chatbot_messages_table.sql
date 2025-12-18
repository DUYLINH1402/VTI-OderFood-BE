-- Migration tạo bảng chatbot_messages cho hệ thống Chatbot AI
-- Ngày tạo: 2025-10-13
-- Dựa theo cấu trúc entity ChatMessage trong package chatbot

CREATE TABLE chatbot_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL COMMENT 'ID session của cuộc hội thoại chatbot',
    user_id BIGINT NULL COMMENT 'ID người dùng (có thể null nếu là khách vãng lai)',
    message_type ENUM('USER', 'BOT') NOT NULL COMMENT 'Loại tin nhắn: USER - tin nhắn từ người dùng, BOT - tin nhắn từ chatbot',
    message_content TEXT NOT NULL COMMENT 'Nội dung tin nhắn',
    context_used TEXT NULL COMMENT 'Context từ RAG được sử dụng để trả lời (chỉ có khi message_type = BOT)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Thời gian tạo tin nhắn',
    response_time INT NULL COMMENT 'Thời gian phản hồi của chatbot (milliseconds)',
    user_rating INT NULL COMMENT 'Đánh giá từ 1-5 của người dùng cho câu trả lời của bot',

    -- Indexes để tối ưu hiệu năng truy vấn
    INDEX idx_session_id (session_id),
    INDEX idx_user_id (user_id),
    INDEX idx_message_type (message_type),
    INDEX idx_created_at (created_at),
    INDEX idx_session_created (session_id, created_at),

    -- Constraint cho user_rating
    CONSTRAINT chk_user_rating CHECK (user_rating IS NULL OR (user_rating >= 1 AND user_rating <= 5))

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng lưu trữ tin nhắn chat giữa người dùng và chatbot AI';
