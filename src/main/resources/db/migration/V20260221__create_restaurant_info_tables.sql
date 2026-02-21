-- Migration: Tạo bảng lưu trữ thông tin nhà hàng và gallery
-- Date: 2026-02-21

-- Bảng thông tin nhà hàng
CREATE TABLE IF NOT EXISTS restaurant_info (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL COMMENT 'Tên nhà hàng',
    logo_url VARCHAR(500) COMMENT 'URL logo nhà hàng',
    address VARCHAR(500) COMMENT 'Địa chỉ nhà hàng',
    phone_number VARCHAR(20) COMMENT 'Số điện thoại liên hệ',
    video_url VARCHAR(500) COMMENT 'URL video giới thiệu',
    description TEXT COMMENT 'Mô tả chi tiết về nhà hàng',
    opening_hours VARCHAR(100) COMMENT 'Giờ mở cửa',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Thông tin nhà hàng';

-- Bảng gallery hình ảnh nhà hàng
CREATE TABLE IF NOT EXISTS restaurant_gallery (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    restaurant_info_id BIGINT NOT NULL COMMENT 'ID nhà hàng',
    image_url VARCHAR(500) NOT NULL COMMENT 'URL hình ảnh',
    display_order INT DEFAULT 0 COMMENT 'Thứ tự hiển thị',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_gallery_restaurant FOREIGN KEY (restaurant_info_id)
        REFERENCES restaurant_info(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Hình ảnh gallery nhà hàng';

-- Index để tối ưu query
CREATE INDEX idx_restaurant_gallery_order ON restaurant_gallery(restaurant_info_id, display_order);

-- Thêm dữ liệu mẫu cho nhà hàng mặc định
INSERT INTO restaurant_info (id, name, logo_url, address, phone_number, video_url, description, opening_hours)
VALUES (
    1,
    'Đồng Xanh Restaurant',
    'https://res.cloudinary.com/ddia5yfia/image/upload/v1749045931/Dongxanh_logo_olnoa9.webp',
    '1211 Nguyễn Văn Linh, P. Hưng Lợi, Q. Ninh Kiều, TP. Cần Thơ',
    '0988 62 66 00',
    'https://youtu.be/hYh4j_vwoV4?si=8oL98CYwCueHR3TX',
    'Chào mừng đến với Đồng Xanh! Chúng tôi phục vụ các món ăn ngon với nguyên liệu tươi sạch và không gian thoải mái.',
    '07:00 - 22:00'
) ON DUPLICATE KEY UPDATE id = id;

-- Thêm một vài hình ảnh gallery mẫu
INSERT INTO restaurant_gallery (restaurant_info_id, image_url, display_order) VALUES
(1, 'https://res.cloudinary.com/ddia5yfia/image/upload/v1771645840/dongxanh_info_gqlkb2.webp', 1),
(1, 'https://res.cloudinary.com/ddia5yfia/image/upload/v1771120542/ve-can-tho-nhat-dinh-phai-ghe-quan-dong-xanh-2_ek7hwh.jpg', 2),
(1, 'https://res.cloudinary.com/ddia5yfia/image/upload/v1769232875/gioi-thieu-3_glkk9g.png', 3),
(1, 'https://res.cloudinary.com/ddia5yfia/image/upload/v1769232875/gioi-thieu_pannxf.png', 4);

