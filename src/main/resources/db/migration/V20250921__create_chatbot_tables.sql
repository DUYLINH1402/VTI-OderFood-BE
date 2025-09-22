-- Migration tạo bảng chat_messages và knowledge_base cho hệ thống Chatbot RAG
-- Ngày tạo: 2025-09-21

-- Tạo bảng knowledge_base để lưu trữ dữ liệu cho hệ thống RAG
CREATE TABLE knowledge_base (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(500) NOT NULL COMMENT 'Tiêu đề của knowledge',
    content TEXT NOT NULL COMMENT 'Nội dung chi tiết',
    keywords VARCHAR(1000) COMMENT 'Từ khóa tìm kiếm, cách nhau bởi dấu phẩy',
    category ENUM(
        'RESTAURANT_INFO',
        'MENU_INFO',
        'ORDER_POLICY',
        'PAYMENT_INFO',
        'DELIVERY_INFO',
        'PROMOTION',
        'FAQ',
        'CONTACT',
        'OPERATING_HOURS',
        'OTHER'
    ) NOT NULL COMMENT 'Danh mục của knowledge',
    priority INT DEFAULT 1 COMMENT 'Độ ưu tiên từ 1-10',
    is_active BOOLEAN DEFAULT TRUE COMMENT 'Trạng thái hoạt động',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL,
    created_by BIGINT COMMENT 'ID admin tạo ra',

    INDEX idx_category (category),
    INDEX idx_is_active (is_active),
    INDEX idx_priority (priority),
    INDEX idx_created_at (created_at),
    FULLTEXT idx_search_content (title, content, keywords)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Bảng lưu trữ knowledge base cho hệ thống RAG';

-- Tạo bảng chat_messages để lưu trữ lịch sử chat
CREATE TABLE chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL COMMENT 'ID session của cuộc hội thoại',
    user_id BIGINT NULL COMMENT 'ID người dùng (có thể null nếu là khách vãng lai)',
    message_type ENUM('USER', 'BOT') NOT NULL COMMENT 'Loại tin nhắn',
    message_content TEXT NOT NULL COMMENT 'Nội dung tin nhắn',
    context_used TEXT COMMENT 'Context từ RAG được sử dụng để trả lời',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    response_time INT COMMENT 'Thời gian phản hồi (milliseconds)',
    user_rating INT COMMENT 'Đánh giá từ 1-5 của người dùng',

    INDEX idx_session_id (session_id),
    INDEX idx_user_id (user_id),
    INDEX idx_message_type (message_type),
    INDEX idx_created_at (created_at),
    INDEX idx_session_created (session_id, created_at),

    CONSTRAINT fk_chat_messages_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Bảng lưu trữ lịch sử chat giữa người dùng và chatbot';
