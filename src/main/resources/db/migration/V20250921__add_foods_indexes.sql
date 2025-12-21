-- Migration: Thêm index cho bảng foods để tối ưu hiệu năng truy vấn
-- Các index này hỗ trợ các truy vấn lọc và tìm kiếm trong trang quản lý Staff

-- Index cho cột name (hỗ trợ tìm kiếm theo tên)
CREATE INDEX IF NOT EXISTS idx_foods_name ON foods(name);

-- Index cho cột status (hỗ trợ lọc theo trạng thái)
CREATE INDEX IF NOT EXISTS idx_foods_status ON foods(status);

-- Index cho cột category_id (hỗ trợ lọc theo danh mục)
CREATE INDEX IF NOT EXISTS idx_foods_category_id ON foods(category_id);

-- Index cho cột is_active (hỗ trợ lọc theo trạng thái hoạt động)
CREATE INDEX IF NOT EXISTS idx_foods_is_active ON foods(is_active);

-- Index kết hợp cho các truy vấn phổ biến
CREATE INDEX IF NOT EXISTS idx_foods_category_status ON foods(category_id, status);
CREATE INDEX IF NOT EXISTS idx_foods_category_active ON foods(category_id, is_active);

-- Index cho các cột boolean thường dùng trong filter
CREATE INDEX IF NOT EXISTS idx_foods_is_new ON foods(is_new);
CREATE INDEX IF NOT EXISTS idx_foods_is_featured ON foods(is_featured);
CREATE INDEX IF NOT EXISTS idx_foods_is_best_seller ON foods(is_best_seller);

-- Index cho cột slug (hỗ trợ tìm kiếm theo slug)
CREATE INDEX IF NOT EXISTS idx_foods_slug ON foods(slug);

