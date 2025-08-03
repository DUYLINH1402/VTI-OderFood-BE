-- Migration: Upgrade Coupon System to Full Features
-- File: upgrade_coupon_system.sql

USE food_ordering_system;

-- 1. Thêm các trường mới vào bảng coupons
ALTER TABLE coupons
ADD COLUMN title VARCHAR(100) AFTER code,
ADD COLUMN min_order_amount DOUBLE AFTER discount_value,
ADD COLUMN max_discount_amount DOUBLE AFTER min_order_amount,
ADD COLUMN max_usage_per_user INT AFTER max_discount_amount,
ADD COLUMN coupon_type VARCHAR(20) NOT NULL DEFAULT 'PUBLIC' AFTER status,
ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER coupon_type,
ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;

-- 2. Cập nhật độ dài description
ALTER TABLE coupons MODIFY COLUMN description VARCHAR(500);

-- 3. Sửa lại các trường datetime hiện có để tránh zero date
UPDATE coupons SET start_date = CURRENT_TIMESTAMP WHERE start_date = '0000-00-00 00:00:00' OR start_date IS NULL;
UPDATE coupons SET end_date = DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 1 YEAR) WHERE end_date = '0000-00-00 00:00:00' OR end_date IS NULL;

-- 4. Tạo bảng coupon_usage với default timestamps
CREATE TABLE IF NOT EXISTS coupon_usage (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    coupon_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    order_id BIGINT NOT NULL,
    discount_amount DOUBLE NOT NULL,
    used_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_coupon_user (coupon_id, user_id),
    INDEX idx_used_at (used_at)
);

-- 5. Tạo bảng coupon_users
CREATE TABLE IF NOT EXISTS coupon_users (
    coupon_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (coupon_id, user_id),
    FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- 6. Tạo bảng coupon_categories nếu chưa có
CREATE TABLE IF NOT EXISTS coupon_categories (
    coupon_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    PRIMARY KEY (coupon_id, category_id),
    FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE CASCADE,
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);

-- 7. Tạo bảng coupon_foods nếu chưa có
CREATE TABLE IF NOT EXISTS coupon_foods (
    coupon_id BIGINT NOT NULL,
    food_id BIGINT NOT NULL,
    PRIMARY KEY (coupon_id, food_id),
    FOREIGN KEY (coupon_id) REFERENCES coupons(id) ON DELETE CASCADE,
    FOREIGN KEY (food_id) REFERENCES foods(id) ON DELETE CASCADE
);

-- 8. Thêm index cho hiệu năng
ALTER TABLE coupons
ADD INDEX IF NOT EXISTS idx_code (code),
ADD INDEX IF NOT EXISTS idx_status_type (status, coupon_type),
ADD INDEX IF NOT EXISTS idx_dates (start_date, end_date),
ADD INDEX IF NOT EXISTS idx_created_at (created_at);

-- 9. Insert sample data với datetime hợp lệ
INSERT IGNORE INTO coupons (
    code, title, description, discount_type, discount_value,
    min_order_amount, max_discount_amount, max_usage_per_user,
    start_date, end_date, max_usage, used_count, status, coupon_type
) VALUES
-- Public Coupons
('SAVE20', 'Giảm 20%', 'Giảm 20% cho đơn hàng từ 100k', 'PERCENT', 20.0,
 100000.0, 50000.0, 3, NOW(), DATE_ADD(NOW(), INTERVAL 6 MONTH),
 1000, 0, 'ACTIVE', 'PUBLIC'),

('FLASH50', 'Flash Sale 50%', 'Flash sale giảm 50% cho đơn từ 200k', 'PERCENT', 50.0,
 200000.0, 100000.0, 1, NOW(), DATE_ADD(NOW(), INTERVAL 1 MONTH),
 500, 0, 'ACTIVE', 'PUBLIC'),

('FREESHIP', 'Miễn phí ship', 'Miễn phí giao hàng cho đơn từ 50k', 'AMOUNT', 25000.0,
 50000.0, NULL, 5, NOW(), DATE_ADD(NOW(), INTERVAL 1 YEAR),
 10000, 0, 'ACTIVE', 'PUBLIC'),

('SUMMER100', 'Summer Sale', 'Giảm 100k cho đơn từ 500k', 'AMOUNT', 100000.0,
 500000.0, NULL, 1, NOW(), DATE_ADD(NOW(), INTERVAL 2 MONTH),
 200, 0, 'ACTIVE', 'PUBLIC');

-- 10. Query kiểm tra sau khi migration
SELECT
    c.id,
    c.code,
    c.title,
    c.discount_type,
    c.discount_value,
    c.status,
    c.start_date,
    c.end_date,
    c.created_at
FROM coupons c
ORDER BY c.created_at DESC
LIMIT 5;
