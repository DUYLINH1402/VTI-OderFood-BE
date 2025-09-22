-- =====================================================
-- Migration: Restructure Order Pricing Fields
-- Date: 2025-09-21
-- Purpose: Add new clear pricing structure to orders table
-- =====================================================

-- STEP 1: Add new pricing columns with clear names
ALTER TABLE orders
ADD COLUMN subtotal_amount DECIMAL(38,2) COMMENT 'Tổng tiền món ăn (không bao gồm phí ship, chưa trừ giảm giá)',
ADD COLUMN shipping_fee DECIMAL(10,2) DEFAULT 0 COMMENT 'Phí giao hàng',
ADD COLUMN total_before_discount DECIMAL(38,2) COMMENT 'Tổng tiền sau khi cộng phí ship, trước khi áp dụng giảm giá',
ADD COLUMN final_amount DECIMAL(38,2) COMMENT 'Số tiền cuối cùng khách phải trả (sau tất cả giảm giá)';

-- STEP 2: Add new discount columns with clear names
ALTER TABLE orders
ADD COLUMN points_used INT DEFAULT 0 COMMENT 'Số điểm thưởng đã sử dụng',
ADD COLUMN points_discount_amount DECIMAL(10,2) DEFAULT 0 COMMENT 'Số tiền giảm từ điểm thưởng';

-- STEP 3: Migrate data from old columns to new columns
-- NOTE: This assumes the following mapping based on your description:
-- - total_food_price was the only correct field (becomes subtotal_amount)
-- - original_amount was actually the final amount customer pays (becomes final_amount)
-- - total_price was confusing (was subtracting extra discounts)
-- - discount_amount becomes points_used

-- Migrate subtotal_amount from total_food_price (the only correct field)
UPDATE orders
SET subtotal_amount = COALESCE(total_food_price, 0)
WHERE subtotal_amount IS NULL;

-- Migrate final_amount from original_amount (actual amount customer pays)
UPDATE orders
SET final_amount = COALESCE(original_amount, total_price, subtotal_amount)
WHERE final_amount IS NULL;

-- Migrate points_used from discount_amount
UPDATE orders
SET points_used = COALESCE(discount_amount, 0)
WHERE points_used IS NULL;

-- Calculate points_discount_amount based on points_used (1 point = 1000 VND)
UPDATE orders
SET points_discount_amount = points_used * 1000
WHERE points_discount_amount IS NULL OR points_discount_amount = 0;

-- Set shipping_fee to 0 initially (can be updated later based on business logic)
UPDATE orders
SET shipping_fee = 0
WHERE shipping_fee IS NULL;

-- Calculate total_before_discount = subtotal_amount + shipping_fee
UPDATE orders
SET total_before_discount = subtotal_amount + COALESCE(shipping_fee, 0)
WHERE total_before_discount IS NULL;

-- STEP 4: Handle edge cases and data consistency
-- Ensure final_amount is not greater than total_before_discount unless there are negative discounts
UPDATE orders
SET final_amount = total_before_discount
WHERE final_amount > total_before_discount
AND (coupon_discount_amount IS NULL OR coupon_discount_amount = 0)
AND (points_discount_amount IS NULL OR points_discount_amount = 0);

-- Ensure final_amount is not negative
UPDATE orders
SET final_amount = 0
WHERE final_amount < 0;

-- STEP 5: Set NOT NULL constraints after data migration
ALTER TABLE orders
MODIFY COLUMN subtotal_amount DECIMAL(38,2) NOT NULL,
MODIFY COLUMN final_amount DECIMAL(38,2) NOT NULL;

-- STEP 6: Add indexes for performance on new columns
CREATE INDEX idx_orders_subtotal_amount ON orders(subtotal_amount);
CREATE INDEX idx_orders_final_amount ON orders(final_amount);
CREATE INDEX idx_orders_points_used ON orders(points_used);

-- STEP 7: Add check constraints to ensure data integrity
ALTER TABLE orders
ADD CONSTRAINT chk_orders_subtotal_positive
    CHECK (subtotal_amount >= 0),
ADD CONSTRAINT chk_orders_shipping_fee_positive
    CHECK (shipping_fee >= 0),
ADD CONSTRAINT chk_orders_final_amount_positive
    CHECK (final_amount >= 0),
ADD CONSTRAINT chk_orders_points_used_positive
    CHECK (points_used >= 0),
ADD CONSTRAINT chk_orders_points_discount_positive
    CHECK (points_discount_amount >= 0);

-- STEP 8: Create a view for backward compatibility (optional)
CREATE OR REPLACE VIEW orders_legacy_view AS
SELECT
    id,
    user_id,
    receiver_name,
    receiver_phone,
    receiver_email,
    delivery_address,
    payment_method,
    delivery_type,
    status,

    -- New clear fields
    subtotal_amount,
    shipping_fee,
    total_before_discount,
    final_amount,
    points_used,
    points_discount_amount,
    coupon_code,
    coupon_discount_amount,

    -- Legacy fields for backward compatibility
    subtotal_amount as total_food_price,
    final_amount as total_price,
    points_used as discount_amount,
    total_before_discount as original_amount,

    created_at,
    updated_at,
    district_id,
    ward_id,
    payment_status,
    payment_time,
    payment_transaction_id,
    staff_note,
    internal_note,
    cancel_reason,
    cancelled_at,
    order_code
FROM orders;

-- STEP 9: Add comments for documentation
ALTER TABLE orders
MODIFY COLUMN total_food_price DECIMAL(38,2) COMMENT 'DEPRECATED: Use subtotal_amount instead',
MODIFY COLUMN total_price DECIMAL(38,2) COMMENT 'DEPRECATED: Use final_amount instead',
MODIFY COLUMN discount_amount INT COMMENT 'DEPRECATED: Use points_used instead',
MODIFY COLUMN original_amount DECIMAL(10,2) COMMENT 'DEPRECATED: Use total_before_discount instead';

-- VERIFICATION QUERY (run after migration to check data)
-- SELECT
--     COUNT(*) as total_orders,
--     COUNT(CASE WHEN subtotal_amount IS NOT NULL THEN 1 END) as has_subtotal,
--     COUNT(CASE WHEN final_amount IS NOT NULL THEN 1 END) as has_final,
--     AVG(subtotal_amount) as avg_subtotal,
--     AVG(final_amount) as avg_final
-- FROM orders;
