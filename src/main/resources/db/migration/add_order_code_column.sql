-- Migration để thêm cột order_code nếu chưa tồn tại và cập nhật giá trị cho các đơn hàng hiện tại

-- Thêm cột order_code nếu chưa tồn tại
ALTER TABLE orders
ADD COLUMN IF NOT EXISTS order_code VARCHAR(50) UNIQUE;

-- Tạo index cho order_code để tối ưu tìm kiếm
CREATE INDEX IF NOT EXISTS idx_orders_order_code ON orders(order_code);

-- Cập nhật order_code cho các đơn hàng hiện tại chưa có mã
UPDATE orders
SET order_code = CONCAT('ORD', LPAD(id, 8, '0'))
WHERE order_code IS NULL OR order_code = '';

-- Đảm bảo order_code không được phép NULL cho các bản ghi mới
ALTER TABLE orders
MODIFY COLUMN order_code VARCHAR(50) NOT NULL;
