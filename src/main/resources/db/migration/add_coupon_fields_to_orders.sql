-- Migration: Add Coupon fields to Orders table
-- File: add_coupon_fields_to_orders.sql

USE food_ordering_system;

-- Thêm các trường coupon vào bảng orders
ALTER TABLE orders
ADD COLUMN coupon_code VARCHAR(50) AFTER discount_amount,
ADD COLUMN coupon_discount_amount DECIMAL(10,2) AFTER coupon_code,
ADD COLUMN original_amount DECIMAL(10,2) AFTER coupon_discount_amount;

-- Thêm index cho coupon_code để tìm kiếm nhanh
ALTER TABLE orders ADD INDEX idx_coupon_code (coupon_code);

-- Cập nhật dữ liệu hiện có (nếu có)
UPDATE orders
SET original_amount = total_price
WHERE original_amount IS NULL AND total_price IS NOT NULL;

SELECT 'Migration completed: Added coupon fields to orders table' as status;
