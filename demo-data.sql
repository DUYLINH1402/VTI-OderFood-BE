-- Demo Data cho Coupon Management System
-- Chạy file này sau khi đã tạo tables

-- Insert sample coupons
INSERT INTO coupons (code, title, description, discount_type, discount_value, max_discount_amount, min_order_amount, max_usage_per_user, start_date, end_date, max_usage, used_count, status, coupon_type, created_at, updated_at) VALUES

-- Public coupons (ai cũng dùng được)
('SAVE20', 'Giảm 20%', 'Giảm 20% cho đơn hàng từ 100k', 'PERCENT', 20.0, 50000.0, 100000.0, 3, '2024-08-01 00:00:00', '2024-12-31 23:59:59', 1000, 45, 'ACTIVE', 'PUBLIC', NOW(), NOW()),

('FLASH50', 'Flash Sale 50%', 'Flash sale giảm 50% cho đơn từ 200k', 'PERCENT', 50.0, 100000.0, 200000.0, 1, '2024-08-01 00:00:00', '2024-08-15 23:59:59', 500, 234, 'ACTIVE', 'PUBLIC', NOW(), NOW()),

('FREESHIP', 'Miễn phí ship', 'Miễn phí giao hàng cho đơn từ 50k', 'AMOUNT', 25000.0, NULL, 50000.0, 5, '2024-08-01 00:00:00', '2024-12-31 23:59:59', 10000, 1234, 'ACTIVE', 'PUBLIC', NOW(), NOW()),

('SUMMER100', 'Summer Sale', 'Giảm 100k cho đơn từ 500k', 'AMOUNT', 100000.0, NULL, 500000.0, 1, '2024-08-01 00:00:00', '2024-08-31 23:59:59', 200, 89, 'ACTIVE', 'PUBLIC', NOW(), NOW()),

-- Welcome coupons (cho user mới)
('WELCOME1', 'Chào mừng bạn mới!', 'Giảm 30k cho đơn đầu tiên', 'AMOUNT', 30000.0, NULL, 100000.0, 1, '2024-08-01 00:00:00', '2024-08-08 23:59:59', 1, 0, 'ACTIVE', 'FIRST_ORDER', NOW(), NOW()),

('WELCOME2', 'Chào mừng bạn mới!', 'Giảm 30k cho đơn đầu tiên', 'AMOUNT', 30000.0, NULL, 100000.0, 1, '2024-08-01 00:00:00', '2024-08-08 23:59:59', 1, 0, 'ACTIVE', 'FIRST_ORDER', NOW(), NOW()),

-- Birthday coupons
('BIRTHDAY1_2024', '🎉 Happy Birthday!', 'Coupon sinh nhật đặc biệt', 'PERCENT', 25.0, 80000.0, 80000.0, 1, '2024-08-01 00:00:00', '2024-08-31 23:59:59', 1, 0, 'ACTIVE', 'BIRTHDAY', NOW(), NOW()),

-- Expired coupons (để test)
('EXPIRED01', 'Coupon đã hết hạn', 'Test expired coupon', 'PERCENT', 15.0, 30000.0, 50000.0, 2, '2024-07-01 00:00:00', '2024-07-31 23:59:59', 100, 67, 'EXPIRED', 'PUBLIC', NOW(), NOW()),

-- Used out coupons (để test)
('USEDOUT01', 'Coupon hết lượt', 'Test used out coupon', 'AMOUNT', 20000.0, NULL, 100000.0, 1, '2024-08-01 00:00:00', '2024-12-31 23:59:59', 50, 50, 'USED_OUT', 'PUBLIC', NOW(), NOW()),

-- Inactive coupons (để test admin functions)
('INACTIVE01', 'Coupon tạm ngừng', 'Test inactive coupon', 'PERCENT', 10.0, 25000.0, 75000.0, 3, '2024-08-01 00:00:00', '2024-12-31 23:59:59', 300, 15, 'INACTIVE', 'PUBLIC', NOW(), NOW());

-- Insert sample coupon usage (giả sử có user id 1, 2, 3 và order id 1, 2, 3)
INSERT INTO coupon_usage (coupon_id, user_id, order_id, discount_amount, used_at) VALUES
(1, 1, 1, 20000.0, '2024-08-01 10:30:00'),
(1, 2, 2, 25000.0, '2024-08-01 14:15:00'),
(2, 1, 3, 50000.0, '2024-08-02 09:20:00'),
(3, 3, 4, 25000.0, '2024-08-02 16:45:00'),
(4, 2, 5, 100000.0, '2024-08-03 11:10:00');

-- Giả sử có categories với id 1, 2, 3 (Fast Food, Beverages, Desserts)
-- Liên kết coupon với categories
INSERT INTO coupon_categories (coupon_id, category_id) VALUES
(1, 1), -- SAVE20 áp dụng cho Fast Food
(1, 2), -- SAVE20 áp dụng cho Beverages
(2, 1), -- FLASH50 áp dụng cho Fast Food
(4, 3); -- SUMMER100 áp dụng cho Desserts

-- Giả sử có foods với id 1, 2, 3, 4, 5
-- Liên kết coupon với specific foods
INSERT INTO coupon_foods (coupon_id, food_id) VALUES
(3, 1), -- FREESHIP áp dụng cho food id 1
(3, 2), -- FREESHIP áp dụng cho food id 2
(3, 3); -- FREESHIP áp dụng cho food id 3

-- Giả sử có users với id 1, 2, 3
-- Liên kết private coupons với users
INSERT INTO coupon_users (coupon_id, user_id) VALUES
(5, 1), -- WELCOME1 chỉ cho user 1
(6, 2), -- WELCOME2 chỉ cho user 2
(7, 1); -- BIRTHDAY1_2024 chỉ cho user 1

-- Query để kiểm tra data
SELECT
    c.code,
    c.title,
    c.discount_type,
    c.discount_value,
    c.status,
    c.coupon_type,
    c.used_count,
    c.max_usage,
    CASE
        WHEN c.used_count >= c.max_usage THEN 'FULL'
        WHEN c.end_date < NOW() THEN 'EXPIRED'
        WHEN c.status = 'ACTIVE' THEN 'AVAILABLE'
        ELSE 'NOT_AVAILABLE'
    END as availability_status
FROM coupons c
ORDER BY c.created_at DESC;
