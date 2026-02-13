    -- Migration: Thêm trường is_protected cho Blog, BlogCategory, Food và User
-- Mục đích: Bảo vệ dữ liệu quan trọng, chỉ SUPER_ADMIN mới có quyền sửa/xóa

-- Thêm cột is_protected vào bảng blogs (nếu chưa tồn tại)
-- MySQL 8.0+ hỗ trợ IF NOT EXISTS cho ADD COLUMN
ALTER TABLE blogs ADD COLUMN is_protected BOOLEAN DEFAULT FALSE;

-- Set tất cả dữ liệu hiện tại là is_protected = TRUE
UPDATE blogs SET is_protected = TRUE WHERE is_protected = FALSE OR is_protected IS NULL;

-- Thêm cột is_protected vào bảng blog_categories
ALTER TABLE blog_categories ADD COLUMN is_protected BOOLEAN DEFAULT FALSE;

-- Set tất cả dữ liệu hiện tại là is_protected = TRUE
UPDATE blog_categories SET is_protected = TRUE WHERE is_protected = FALSE OR is_protected IS NULL;

-- Thêm cột is_protected vào bảng foods
ALTER TABLE foods ADD COLUMN is_protected BOOLEAN DEFAULT FALSE;

-- Set tất cả dữ liệu hiện tại là is_protected = TRUE
UPDATE foods SET is_protected = TRUE WHERE is_protected = FALSE OR is_protected IS NULL;

-- Thêm cột is_protected vào bảng users
ALTER TABLE users ADD COLUMN is_protected BOOLEAN DEFAULT FALSE;

-- Set tất cả dữ liệu hiện tại là is_protected = TRUE
UPDATE users SET is_protected = TRUE WHERE is_protected = FALSE OR is_protected IS NULL;

-- Thêm role SUPER_ADMIN (nếu chưa tồn tại)
INSERT INTO roles (name, code)
SELECT 'Quản trị viên cao cấp', 'ROLE_SUPER_ADMIN'
FROM (SELECT 1) AS tmp
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE code = 'ROLE_SUPER_ADMIN'
) LIMIT 1;

-- Tạo index cho trường is_protected để tối ưu query
CREATE INDEX idx_blogs_is_protected ON blogs(is_protected);
CREATE INDEX idx_blog_categories_is_protected ON blog_categories(is_protected);
CREATE INDEX idx_foods_is_protected ON foods(is_protected);
CREATE INDEX idx_users_is_protected ON users(is_protected);

