-- Migration tạo bảng notifications hỗ trợ cả User và Staff
-- File: create_notifications_table.sql

CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,                    -- Có thể null nếu là thông báo cho staff
    order_id BIGINT NULL,
    order_code VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,

    -- Thêm các trường mới để hỗ trợ Staff notifications
    recipient_type ENUM('USER', 'STAFF', 'ADMIN') NOT NULL DEFAULT 'USER',
    recipient_id BIGINT NOT NULL,           -- ID của người nhận (user_id hoặc staff_id)

    is_read BOOLEAN DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    read_at DATETIME NULL,

    -- Foreign key constraints
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_notifications_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Tạo index để tối ưu hiệu năng truy vấn
CREATE INDEX idx_notifications_recipient ON notifications(recipient_type, recipient_id);
CREATE INDEX idx_notifications_recipient_read ON notifications(recipient_type, recipient_id, is_read);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_order_id ON notifications(order_id);

-- Index compound cho các truy vấn phổ biến
CREATE INDEX idx_notifications_user_status ON notifications(recipient_type, recipient_id, is_read, created_at);
CREATE INDEX idx_notifications_staff_status ON notifications(recipient_type, recipient_id, is_read, created_at);
