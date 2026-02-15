-- Migration: Add blog_type and content-specific fields to blogs table
-- Date: 2026-02-15
-- Description: Mở rộng bảng blogs để hỗ trợ 3 loại nội dung:
--   1. NEWS_PROMOTIONS: Tin tức nội bộ, khuyến mãi (Blog truyền thống)
--   2. MEDIA_PRESS: Báo chí nói về nhà hàng (có link gốc, tên nguồn)
--   3. CATERING_SERVICES: Dịch vụ đãi tiệc lưu động (showcase gói tiệc, thực đơn, hình ảnh)

-- ===========================================
-- 1. Thêm cột blog_type vào bảng blogs
-- ===========================================
ALTER TABLE blogs
ADD COLUMN blog_type VARCHAR(30) NOT NULL DEFAULT 'NEWS_PROMOTIONS' AFTER status;

-- Tạo index cho blog_type
CREATE INDEX idx_blogs_blog_type ON blogs(blog_type);

-- ===========================================
-- 2. Thêm các cột cho MEDIA_PRESS
-- ===========================================
-- Link bài báo gốc
ALTER TABLE blogs
ADD COLUMN source_url VARCHAR(500) NULL AFTER tags;

-- Tên nguồn báo chí (ví dụ: VnExpress, Tuổi Trẻ, Dân Trí...)
ALTER TABLE blogs
ADD COLUMN source_name VARCHAR(200) NULL AFTER source_url;

-- Logo nguồn báo chí
ALTER TABLE blogs
ADD COLUMN source_logo VARCHAR(500) NULL AFTER source_name;

-- Ngày đăng bài trên báo gốc
ALTER TABLE blogs
ADD COLUMN source_published_at DATETIME NULL AFTER source_logo;

-- ===========================================
-- 3. Thêm các cột cho CATERING_SERVICES
-- ===========================================
-- Khoảng giá dịch vụ (ví dụ: "500.000 - 2.000.000 VNĐ/người")
ALTER TABLE blogs
ADD COLUMN price_range VARCHAR(200) NULL AFTER source_published_at;

-- Khu vực phục vụ (lưu dạng JSON hoặc comma-separated)
ALTER TABLE blogs
ADD COLUMN service_areas VARCHAR(1000) NULL AFTER price_range;

-- Danh sách món ăn trong gói tiệc (lưu dạng JSON)
ALTER TABLE blogs
ADD COLUMN menu_items TEXT NULL AFTER service_areas;

-- Gallery hình ảnh thực tế (lưu dạng JSON array các URL)
ALTER TABLE blogs
ADD COLUMN gallery_images TEXT NULL AFTER menu_items;

-- Sức chứa tối thiểu (số người)
ALTER TABLE blogs
ADD COLUMN min_capacity INT NULL AFTER gallery_images;

-- Sức chứa tối đa (số người)
ALTER TABLE blogs
ADD COLUMN max_capacity INT NULL AFTER min_capacity;

-- Thông tin liên hệ đặt tiệc (số điện thoại hoặc email riêng)
ALTER TABLE blogs
ADD COLUMN contact_info VARCHAR(500) NULL AFTER max_capacity;

-- ===========================================
-- 4. Thêm cột blog_type vào bảng blog_categories
-- ===========================================
ALTER TABLE blog_categories
ADD COLUMN blog_type VARCHAR(30) NOT NULL DEFAULT 'NEWS_PROMOTIONS' AFTER description;

-- Tạo index cho blog_type trong blog_categories
CREATE INDEX idx_blog_categories_blog_type ON blog_categories(blog_type);

-- ===========================================
-- 5. Cập nhật dữ liệu mặc định (nếu đã có dữ liệu)
-- ===========================================
-- Cập nhật tất cả bài viết hiện tại thành NEWS_PROMOTIONS (đã là mặc định trong ALTER)
-- UPDATE blogs SET blog_type = 'NEWS_PROMOTIONS' WHERE blog_type IS NULL;

-- Cập nhật tất cả danh mục hiện tại thành NEWS_PROMOTIONS (đã là mặc định trong ALTER)
-- UPDATE blog_categories SET blog_type = 'NEWS_PROMOTIONS' WHERE blog_type IS NULL;

-- ===========================================
-- ROLLBACK (nếu cần hoàn tác)
-- ===========================================
-- ALTER TABLE blogs DROP COLUMN blog_type;
-- ALTER TABLE blogs DROP COLUMN source_url;
-- ALTER TABLE blogs DROP COLUMN source_name;
-- ALTER TABLE blogs DROP COLUMN source_logo;
-- ALTER TABLE blogs DROP COLUMN source_published_at;
-- ALTER TABLE blogs DROP COLUMN price_range;
-- ALTER TABLE blogs DROP COLUMN service_areas;
-- ALTER TABLE blogs DROP COLUMN menu_items;
-- ALTER TABLE blogs DROP COLUMN gallery_images;
-- ALTER TABLE blogs DROP COLUMN min_capacity;
-- ALTER TABLE blogs DROP COLUMN max_capacity;
-- ALTER TABLE blogs DROP COLUMN contact_info;
-- ALTER TABLE blog_categories DROP COLUMN blog_type;
-- DROP INDEX idx_blogs_blog_type ON blogs;
-- DROP INDEX idx_blog_categories_blog_type ON blog_categories;

