-- Migration để tạo bảng mới cho chat giữa User và Staff
-- File: V20241013_02__create_user_staff_chat_messages_table.sql
-- LƯU Ý: Không xóa bảng chat_messages vì đó là của Chatbot AI

-- Tạo bảng conversation để quản lý cuộc trò chuyện duy nhất giữa user và staff
CREATE TABLE user_staff_conversations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_message_at DATETIME NULL,
    is_active BOOLEAN DEFAULT TRUE,
    staff_notes TEXT NULL COMMENT 'Ghi chú của staff về cuộc trò chuyện',

    CONSTRAINT fk_conversation_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    INDEX idx_conversation_user_id (user_id),
    INDEX idx_conversation_active (is_active),
    INDEX idx_conversation_last_message (last_message_at)
);

-- Tạo bảng mới cho chat User-Staff với tên khác để tránh conflict với Chatbot
CREATE TABLE user_staff_chat_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_id VARCHAR(255) NOT NULL UNIQUE,
    conversation_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT NULL,
    message_type ENUM('USER_TO_STAFF', 'STAFF_TO_USER') NOT NULL,
    sent_at DATETIME NOT NULL,
    read_at DATETIME NULL,
    status ENUM('SENT', 'DELIVERED', 'READ') NOT NULL DEFAULT 'SENT',
    session_id VARCHAR(255) NULL,

    -- Soft Delete fields
    is_deleted_by_user BOOLEAN DEFAULT FALSE COMMENT 'User đã xóa tin nhắn này (soft delete)',
    is_deleted_by_staff BOOLEAN DEFAULT FALSE COMMENT 'Staff đã xóa tin nhắn này (soft delete)',
    deleted_by_user_at DATETIME NULL COMMENT 'Thời gian user xóa',
    deleted_by_staff_at DATETIME NULL COMMENT 'Thời gian staff xóa',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Foreign key constraints
    CONSTRAINT fk_user_staff_chat_conversation
        FOREIGN KEY (conversation_id) REFERENCES user_staff_conversations(id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_user_staff_chat_sender
        FOREIGN KEY (sender_id) REFERENCES users(id)
        ON DELETE CASCADE ON UPDATE CASCADE,

    CONSTRAINT fk_user_staff_chat_receiver
        FOREIGN KEY (receiver_id) REFERENCES users(id)
        ON DELETE SET NULL ON UPDATE CASCADE,

    -- Indexes để tối ưu hiệu suất
    INDEX idx_user_staff_message_id (message_id),
    INDEX idx_user_staff_conversation_id (conversation_id),
    INDEX idx_user_staff_sender_id (sender_id),
    INDEX idx_user_staff_receiver_id (receiver_id),
    INDEX idx_user_staff_message_type (message_type),
    INDEX idx_user_staff_sent_at (sent_at),
    INDEX idx_user_staff_status (status),
    INDEX idx_user_staff_session_id (session_id),
    INDEX idx_user_staff_deleted_by_user (is_deleted_by_user),
    INDEX idx_user_staff_deleted_by_staff (is_deleted_by_staff)
);

-- Thêm comment cho các bảng
ALTER TABLE user_staff_conversations COMMENT = 'Bảng quản lý cuộc trò chuyện duy nhất giữa User và Staff';
ALTER TABLE user_staff_chat_messages COMMENT = 'Bảng lưu trữ tin nhắn chat giữa User và Staff (khác với chat_messages của Chatbot AI)';

-- Comment cho các cột của bảng conversations
ALTER TABLE user_staff_conversations MODIFY COLUMN user_id BIGINT NOT NULL UNIQUE COMMENT 'ID user (mỗi user chỉ có 1 conversation)';
ALTER TABLE user_staff_conversations MODIFY COLUMN last_message_at DATETIME NULL COMMENT 'Thời gian tin nhắn cuối cùng';
ALTER TABLE user_staff_conversations MODIFY COLUMN is_active BOOLEAN DEFAULT TRUE COMMENT 'Trạng thái hoạt động của cuộc trò chuyện';

-- Comment cho các cột của bảng messages
ALTER TABLE user_staff_chat_messages MODIFY COLUMN message_id VARCHAR(255) NOT NULL UNIQUE COMMENT 'UUID của tin nhắn';
ALTER TABLE user_staff_chat_messages MODIFY COLUMN conversation_id BIGINT NOT NULL COMMENT 'ID cuộc trò chuyện';
ALTER TABLE user_staff_chat_messages MODIFY COLUMN content TEXT NOT NULL COMMENT 'Nội dung tin nhắn';
ALTER TABLE user_staff_chat_messages MODIFY COLUMN sender_id BIGINT NOT NULL COMMENT 'ID người gửi (phải tồn tại trong bảng users)';
ALTER TABLE user_staff_chat_messages MODIFY COLUMN receiver_id BIGINT NULL COMMENT 'ID người nhận (NULL nếu gửi cho tất cả staff)';
ALTER TABLE user_staff_chat_messages MODIFY COLUMN message_type ENUM('USER_TO_STAFF', 'STAFF_TO_USER') NOT NULL COMMENT 'Loại tin nhắn';
ALTER TABLE user_staff_chat_messages MODIFY COLUMN sent_at DATETIME NOT NULL COMMENT 'Thời gian gửi';
ALTER TABLE user_staff_chat_messages MODIFY COLUMN read_at DATETIME NULL COMMENT 'Thời gian đọc';
ALTER TABLE user_staff_chat_messages MODIFY COLUMN status ENUM('SENT', 'DELIVERED', 'READ') NOT NULL DEFAULT 'SENT' COMMENT 'Trạng thái tin nhắn';
ALTER TABLE user_staff_chat_messages MODIFY COLUMN session_id VARCHAR(255) NULL COMMENT 'Session ID của WebSocket';
