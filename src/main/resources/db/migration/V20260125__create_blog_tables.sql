-- Migration: Tạo bảng blog_categories và blogs
-- Version: V20260125
-- Description: Tạo module Blog/Tin tức cho hệ thống

-- Bảng danh mục tin tức
CREATE TABLE IF NOT EXISTS blog_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT 'Tên danh mục',
    slug VARCHAR(150) NOT NULL UNIQUE COMMENT 'Slug URL thân thiện',
    description VARCHAR(500) COMMENT 'Mô tả danh mục',
    display_order INT DEFAULT 0 COMMENT 'Thứ tự hiển thị',
    is_active BOOLEAN DEFAULT TRUE COMMENT 'Trạng thái hoạt động',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_blog_categories_slug (slug),
    INDEX idx_blog_categories_is_active (is_active),
    INDEX idx_blog_categories_display_order (display_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Danh mục tin tức/bài viết';

-- Bảng bài viết/tin tức
CREATE TABLE IF NOT EXISTS blogs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL COMMENT 'Tiêu đề bài viết',
    slug VARCHAR(300) NOT NULL UNIQUE COMMENT 'Slug URL thân thiện SEO',
    summary VARCHAR(500) COMMENT 'Tóm tắt bài viết',
    content LONGTEXT COMMENT 'Nội dung chi tiết (HTML/JSON)',
    thumbnail VARCHAR(500) COMMENT 'URL ảnh đại diện',
    status ENUM('DRAFT', 'PUBLISHED', 'ARCHIVED') NOT NULL DEFAULT 'DRAFT' COMMENT 'Trạng thái bài viết',
    view_count INT DEFAULT 0 COMMENT 'Lượt xem',
    is_featured BOOLEAN DEFAULT FALSE COMMENT 'Bài viết nổi bật',
    tags VARCHAR(500) COMMENT 'Tags phân loại (comma-separated)',
    meta_title VARCHAR(255) COMMENT 'SEO: Meta title',
    meta_description VARCHAR(500) COMMENT 'SEO: Meta description',
    published_at DATETIME COMMENT 'Thời điểm xuất bản (hỗ trợ lên lịch)',
    user_id BIGINT NOT NULL COMMENT 'ID người viết',
    category_id BIGINT COMMENT 'ID danh mục',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_blogs_slug (slug),
    INDEX idx_blogs_status (status),
    INDEX idx_blogs_category (category_id),
    INDEX idx_blogs_published_at (published_at),
    INDEX idx_blogs_is_featured (is_featured),
    INDEX idx_blogs_user (user_id),

    CONSTRAINT fk_blogs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_blogs_category FOREIGN KEY (category_id) REFERENCES blog_categories(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Bài viết/Tin tức';



-- Dữ liệu mẫu cho bảng blog_categories
-- Đảm bảo bảng trống trước khi chèn (tùy chọn)
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE blog_categories;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO blog_categories (id, name, slug, description, display_order, is_active) VALUES
(1, 'Khuyến mãi', 'khuyen-mai', 'Các chương trình khuyến mãi, ưu đãi đặc biệt', 1, TRUE),
(2, 'Review món ăn', 'review-mon-an', 'Đánh giá, giới thiệu các món ăn ngon', 2, TRUE),
(3, 'Tin nội bộ', 'tin-noi-bo', 'Tin tức nội bộ nhà hàng', 3, TRUE),
(4, 'Hướng dẫn', 'huong-dan', 'Hướng dẫn sử dụng dịch vụ', 4, TRUE);

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE blogs;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO blogs (
    title, slug, summary, content, thumbnail, status,
    view_count, is_featured, tags, meta_title, meta_description,
    published_at, user_id, category_id
) VALUES
-- 1. Valentine
(
    'Lễ tình nhân ngọt ngào: Set Menu "Eternal Love" dành riêng cho cặp đôi',
    'le-tinh-nhan-ngot-ngao-set-menu-eternal-love',
    'Khám phá trải nghiệm ẩm thực lãng mạn dưới ánh nến với thực đơn đặc biệt gồm 5 món thượng hạng chỉ có trong dịp Valentine 2026.',
    '<h2>Hương vị của tình yêu vĩnh cửu</h2><p>Chào đón mùa chung đôi, nhà hàng chúng tôi ra mắt set menu <strong>"Eternal Love"</strong> được thiết kế bởi Bếp trưởng Trần Văn An. Đây không chỉ là một bữa ăn, mà là một hành trình cảm xúc được dẫn dắt qua từng hương vị tinh tế.</p><h3>Thực đơn chi tiết bao gồm:</h3><ul><li><strong>Khai vị:</strong> Súp bào ngư vi cá với hương thảo mộc nhẹ nhàng, kích thích vị giác.</li><li><strong>Món chính 1:</strong> Cá hồi Na Uy áp chảo sốt chanh leo, ăn kèm măng tây giòn tan.</li><li><strong>Món chính 2:</strong> Thăn bò Wagyu nướng sốt vang đỏ - điểm nhấn với độ mềm mọng như tan trong miệng.</li><li><strong>Tráng miệng:</strong> Chocolate Lava tan chảy phục vụ cùng dâu tây Đà Lạt tươi và kem vanilla.</li></ul><p><em>Lưu ý: Chương trình chỉ áp dụng từ ngày 13/02 đến hết 15/02. Quý khách vui lòng đặt bàn trước 24h để được tặng kèm 02 ly rượu vang hồng và gói trang trí nến/hoa hồng miễn phí.</em></p>',
    'https://plus.unsplash.com/premium_photo-1702834007369-e5badbdbcba0?q=80&w=800&auto=format&fit=crop',
    'PUBLISHED', 450, TRUE, 'promotion,valentine,event',
    'Set menu Valentine lãng mạn 2026 tại Nhà hàng', 'Thưởng thức bữa tối lãng mạn cùng người thương với ưu đãi đặc biệt tặng rượu vang và trang trí nến hoa miễn phí.',
    '2026-02-01 09:00:00', 56, 1
),
-- 2. Steak Review
(
    'Bí mật đằng sau miếng Steak hoàn hảo: Từ lò ủ Dry-aged đến bàn tiệc',
    'bi-mat-dang-sau-mieng-steak-hoan-hao',
    'Tại sao thịt bò tại nhà hàng lại mềm mọng và có hương vị đặc trưng đến vậy? Cùng khám phá quy trình ủ khô 28 ngày khắt khe.',
    '<h2>Nghệ thuật của thời gian và nhiệt độ</h2><p>Để tạo ra một miếng Steak chuẩn 5 sao, quy trình của chúng tôi bắt đầu từ rất lâu trước khi nó được đặt lên vỉ nướng. Bí mật nằm ở kỹ thuật <strong>Dry-aged (Ủ khô thịt bò)</strong>.</p><h3>Quy trình 28 ngày nghiêm ngặt</h3><p>Thịt bò thăn lưng (Ribeye) được treo trong tủ chuyên dụng với nhiệt độ chuẩn 2°C và độ ẩm 80%. Trong thời gian này, các enzyme tự nhiên sẽ phá vỡ các mô liên kết, giúp thịt trở nên mềm mại và phát triển hương vị đậm đà như mùi hạt dẻ và phô mai nhẹ.</p><h3>Kỹ thuật nướng lửa hồng</h3><p>Chúng tôi sử dụng than hoa không khói để giữ trọn mùi thơm tự nhiên. Nhiệt độ lò nướng luôn được duy trì ở mức 300 độ C để tạo lớp vỏ ngoài cháy xém (crust) thơm lừng mà bên trong vẫn giữ được độ hồng hào, mọng nước chuẩn Medium Rare.</p><p>Hãy đến và cảm nhận sự khác biệt trong từng thớ thịt tại nhà hàng ngay hôm nay!</p>',
    'https://images.unsplash.com/photo-1709433420444-0535a5f616b9?q=80&w=800&auto=format&fit=crop',
    'PUBLISHED', 1200, TRUE, 'review,steak,chef-tips',
    'Bí quyết làm Steak Dry-aged ngon chuẩn vị 5 sao', 'Khám phá quy trình ủ khô thịt bò 28 ngày và kỹ thuật nướng độc quyền từ bếp trưởng của chúng tôi.',
    '2026-01-15 14:30:00', 56, 2
),
-- 3. Chef News
(
    'Chào đón Bếp trưởng mới: Nghệ nhân ẩm thực Trần Văn An và tầm nhìn 2026',
    'chao-don-bep-truong-moi-tran-van-an',
    'Nhà hàng hân hoan chào đón sự gia nhập của Chef Trần Văn An - người mang luồng gió mới đến thực đơn Fusion đặc sắc.',
    '<p>Nhà hàng chúng tôi tự hào thông báo sự gia nhập của <strong>Chef Trần Văn An</strong> trong vai trò Bếp trưởng điều hành. Với hơn 15 năm kinh nghiệm tại các khách sạn 5 sao quốc tế như Marriott và Sheraton, Chef An được biết đến là một nghệ nhân trong việc kết hợp nguyên liệu địa phương với kỹ thuật chế biến phương Tây hiện đại.</p><blockquote>"Ẩm thực không chỉ là ăn ngon, đó là sự kết hợp của nghệ thuật trình bày, cảm xúc và ký ức của thực khách." - Chef An chia sẻ.</blockquote><h3>Dự án thực đơn Fusion 2026</h3><p>Trong quý tới, Chef An sẽ giới thiệu bộ sưu tập món ăn mới mang phong cách Fusion, nơi Phở Việt Nam có thể kết hợp tinh tế với gan ngỗng Pháp, hay nước mắm truyền thống được nâng tầm thành các loại sốt tinh xảo cho hải sản. Chúng tôi tin rằng đây sẽ là một bước ngoặt lớn cho trải nghiệm ẩm thực của quý khách.</p>',
    'https://images.unsplash.com/photo-1572715376701-98568319fd0b?q=80&w=800&auto=format&fit=crop',
    'PUBLISHED', 210, FALSE, 'news,internal,chef',
    'Giới thiệu Bếp trưởng mới Trần Văn An | Tầm nhìn 2026', 'Chào đón bếp trưởng mới với hơn 15 năm kinh nghiệm và dự án thực đơn Fusion độc đáo tại nhà hàng.',
    '2026-01-10 08:00:00', 56, 3
),
-- 4. ZaloPay Guide
(
    'Hướng dẫn đặt món online và thanh toán qua ZaloPay cực nhanh',
    'huong-dan-dat-mon-online-zalo-pay',
    'Trải nghiệm đặt món nhanh chóng, bảo mật và nhận ngay voucher giảm giá khi thanh toán qua ví điện tử ZaloPay.',
    '<h2>Tiện lợi hơn với thanh toán không tiền mặt</h2><p>Nhằm nâng cao trải nghiệm mua sắm, hệ thống đặt món online của nhà hàng đã chính thức tích hợp cổng thanh toán <strong>ZaloPay</strong>. Giờ đây, bạn không cần lo lắng về tiền lẻ hay thanh toán khi giao hàng (COD) nữa.</p><h3>Các bước thực hiện đơn giản:</h3><ol><li><strong>Chọn món:</strong> Truy cập menu trên website, chọn các món ăn yêu thích và nhấn "Thêm vào giỏ hàng".</li><li><strong>Nhập thông tin:</strong> Điền địa chỉ giao hàng và số điện thoại liên hệ.</li><li><strong>Thanh toán:</strong> Tại bước thanh toán, chọn biểu tượng ZaloPay. Hệ thống sẽ tự động mở ứng dụng ZaloPay trên điện thoại của bạn.</li><li><strong>Xác nhận:</strong> Kiểm tra số tiền và xác nhận thanh toán.</li></ol><p><strong>Đặc biệt:</strong> Giảm ngay 20.000đ cho đơn hàng đầu tiên thanh toán qua ZaloPay (Áp dụng cho đơn từ 200.000đ). Thử ngay hôm nay!</p>',
    'https://plus.unsplash.com/premium_photo-1663931932521-0207c53c564b?q=80&w=800&auto=format&fit=crop',
    'PUBLISHED', 890, FALSE, 'tutorial,zalopay,online-order',
    'Hướng dẫn thanh toán ZaloPay khi đặt món online', 'Cách sử dụng ZaloPay để đặt đồ ăn online nhanh chóng và nhận voucher ưu đãi từ nhà hàng.',
    '2026-01-05 10:00:00', 56, 4
),
-- 5. Happy Hour
(
    'Happy Hour: Uống thả ga, không lo về giá từ 16h - 18h',
    'happy-hour-uong-tha-ga-khong-lo-ve-gia',
    'Tận hưởng khung giờ vàng mỗi ngày với ưu đãi Mua 1 Tặng 1 cho toàn bộ menu đồ uống có cồn và Mocktail.',
    '<h2>Giải tỏa căng thẳng sau giờ làm việc</h2><p>Đừng để một ngày làm việc mệt mỏi trôi qua trong lặng lẽ. Hãy ghé ngay nhà hàng chúng tôi để tận hưởng không gian chill và chương trình ưu đãi <strong>Happy Hour</strong> lớn nhất trong ngày.</p><h3>Chi tiết ưu đãi:</h3><ul><li><strong>Thời gian:</strong> 16:00 - 18:00 hàng ngày (kể cả cuối tuần).</li><li><strong>Nội dung:</strong> Mua 1 Tặng 1 cho tất cả các loại Beer thủ công (Craft Beer), Cocktail, và rượu vang theo ly.</li><li><strong>Đặc biệt:</strong> Miễn phí một phần Snack khoai tây chiên muối cho nhóm đi từ 4 người.</li></ul><p>Thưởng thức một ly <em>Classic Mojito</em> mát lạnh hay một ly <em>IPA Beer</em> đậm đà trong không gian âm nhạc Acoustic nhẹ nhàng chắc chắn sẽ giúp bạn tái tạo năng lượng hiệu quả.</p>',
    'https://images.unsplash.com/photo-1767065702845-61619d22c0cd?q=80&w=800&auto=format&fit=crop',
    'PUBLISHED', 315, FALSE, 'promotion,happyhour,drinks',
    'Chương trình Happy Hour Mua 1 Tặng 1 đồ uống', 'Khuyến mãi khung giờ vàng 16h-18h hàng ngày: Mua 1 Tặng 1 đồ uống tại quầy bar nhà hàng.',
    '2026-01-20 16:00:00', 56, 1
),
-- 6. Vegan Review
(
    'Ăn lành sống khỏe với Thực đơn Xanh (Vegan Menu) thượng hạng',
    'an-lanh-song-khoe-voi-thuc-don-xanh',
    'Khám phá hương vị thanh tao nhưng đầy lôi cuốn từ các nguyên liệu thực vật tươi ngon nhất trong thực đơn mới.',
    '<h2>Món chay không hề đơn điệu</h2><p>Tại nhà hàng, chúng tôi định nghĩa lại khái niệm món chay. Không chỉ là rau củ đơn thuần, <strong>Vegan Menu</strong> là sự kết hợp sáng tạo giữa các loại hạt, nấm rừng và thảo mộc để tạo nên những món ăn đầy đủ dinh dưỡng và đậm đà hương vị.</p><h3>Những món "Must-try" trong menu xanh:</h3><ul><li><strong>Salad Quinoa trái bơ:</strong> Sự kết hợp hoàn hảo của hạt siêu thực phẩm Quinoa, bơ sáp Đắk Lắk và sốt chanh leo chua ngọt.</li><li><strong>Nấm rừng sốt Truffle:</strong> Các loại nấm quý được xào nhanh tay với dầu nấm Truffle thơm lừng, ăn kèm với bánh mì nướng giòn.</li><li><strong>Cà ri xanh rau củ:</strong> Vị béo của nước cốt dừa hòa quyện cùng vị cay nhẹ của cà ri Thái và độ ngọt tự nhiên từ rau củ mùa hè.</li></ul><p>Dù bạn là người ăn chay trường hay chỉ đơn giản muốn thay đổi khẩu vị cho nhẹ bụng, thực đơn này chắc chắn sẽ làm bạn hài lòng.</p>',
    'https://plus.unsplash.com/premium_photo-1718822007772-f73b2cd6f5e6?q=80&w=800&auto=format&fit=crop',
    'PUBLISHED', 560, TRUE, 'review,vegan,healthy',
    'Thực đơn chay Healthy - Sống xanh cùng nhà hàng', 'Review các món chay cao cấp như Salad Quinoa và Nấm sốt Truffle dành cho người yêu thích lối sống lành mạnh.',
    '2025-12-25 11:00:00', 56, 2
),
-- 7. Internal News - Award
(
    'Vinh dự nhận giải thưởng "Nhà hàng được yêu thích nhất 2025"',
    'vinh-du-nhan-giai-thuong-nha-hang-yeu-thich-2025',
    'Nhà hàng tự hào được xướng tên tại lễ trao giải Foodies Choice Awards nhờ sự tin yêu của quý khách hàng.',
    '<p>Chúng tôi vô cùng tự hào và xúc động khi được vinh danh tại hạng mục quan trọng nhất của giải thưởng <strong>Foodies Choice Awards 2025</strong>. Đây là kết quả của một năm nỗ lực không ngừng nghỉ từ đội ngũ đầu bếp, nhân viên phục vụ cho đến ban quản lý.</p><h3>Cam kết chất lượng trong năm 2026</h3><p>Giải thưởng này không chỉ là sự công nhận mà còn là trách nhiệm lớn lao. Trong năm 2026, chúng tôi cam kết:</p><ul><li>Duy trì nguồn nguyên liệu sạch, có nguồn gốc rõ ràng (Organic).</li><li>Liên tục đổi mới menu để mang đến những trải nghiệm thú vị.</li><li>Nâng cao chất lượng phục vụ theo tiêu chuẩn quốc tế.</li></ul><p>Xin gửi lời cảm ơn chân thành nhất đến tất cả quý khách hàng đã luôn đồng hành và ủng hộ chúng tôi trong suốt thời gian qua!</p>',
    'https://plus.unsplash.com/premium_photo-1713628398440-9d056ad0d468?q=80&w=800&auto=format&fit=crop',
    'PUBLISHED', 1500, TRUE, 'news,awards,achievement',
    'Nhà hàng đạt giải Foodies Choice Awards 2025', 'Thông báo và tri ân khách hàng khi nhà hàng nhận giải thưởng nhà hàng được yêu thích nhất năm 2025.',
    '2026-01-01 00:00:00', 56, 3
),
-- 8. Birthday Promotion
(
    'Ưu đãi tiệc sinh nhật: Tặng bánh kem và trang trí bàn tiệc miễn phí',
    'uu-dai-sinh-nhat-tang-banh-kem-trang-tri',
    'Biến ngày sinh nhật của bạn thành kỷ niệm khó quên với dịch vụ tổ chức tiệc chuyên nghiệp tại nhà hàng.',
    '<h2>Tiệc sinh nhật lung linh - Không lo chi phí</h2><p>Bạn đang tìm kiếm một địa điểm ấm cúng nhưng không kém phần sang trọng để mừng tuổi mới cùng người thân? Nhà hàng chúng tôi chính là lựa chọn hoàn hảo với gói <strong>Ưu đãi Sinh nhật 2026</strong>.</p><h3>Gói quà tặng bao gồm:</h3><ul><li><strong>Bánh kem thiết kế:</strong> Tặng 01 bánh kem bắp hoặc chocolate cao cấp cho nhóm từ 6 khách.</li><li><strong>Trang trí bàn tiệc:</strong> Miễn phí set-up nến, hoa tươi và bóng bay theo tông màu bạn yêu thích (Hồng, Xanh, Vàng Gold).</li><li><strong>Ưu đãi đồ uống:</strong> Giảm giá 10% cho toàn bộ rượu vang trong bữa tiệc.</li></ul><p><em>Điều kiện áp dụng: Quý khách vui lòng đặt bàn trước ít nhất 48h và mang theo CMND/CCCD để nhân viên xác nhận thông tin ngày sinh nhật.</em></p>',
    'https://images.unsplash.com/photo-1755704282718-1afe3790082b?q=80&w=800&auto=format&fit=crop',
    'PUBLISHED', 420, FALSE, 'promotion,birthday,party',
    'Đặt tiệc sinh nhật trọn gói - Ưu đãi hấp dẫn 2026', 'Dịch vụ tổ chức tiệc sinh nhật miễn phí trang trí và tặng bánh kem cho khách hàng đặt bàn trước.',
    '2026-01-18 09:00:00', 56, 1
),
-- 9. Summer Drinks Review
(
    'Giải nhiệt mùa hè với bộ sưu tập "Tropical Summer Drinks" mới nhất',
    'giai-nhiet-mua-he-voi-tropical-drinks',
    'Đánh bay cái nóng oi ả với 3 loại đồ uống đặc trưng mang hương vị của vùng biển nhiệt đới.',
    '<h2>Hơi thở đại dương trong từng ngụm nước</h2><p>Mùa hè này, quầy Bar của nhà hàng trở nên sôi động hơn bao giờ hết với sự xuất hiện của bộ sưu tập <strong>Tropical Summer Drinks</strong>. Đây là sự kết hợp giữa các loại trái cây tươi mọng và kỹ thuật pha chế hiện đại.</p><h3>3 Siêu phẩm không thể bỏ qua:</h3><ul><li><strong>Summer Sunset:</strong> Một sự pha trộn rực rỡ giữa nước ép cam vắt, siro dâu và soda lạnh, tạo nên màu sắc như buổi hoàng hôn trên biển.</li><li><strong>Lychee Mint Cooler:</strong> Vị ngọt thanh của vải thiều kết hợp với hương bạc hà mát lạnh, cực kỳ sảng khoái.</li><li><strong>Passion Fruit Mojito:</strong> Biến tấu từ dòng Mojito cổ điển với chanh dây tươi, mang lại vị chua thanh khiết và hậu vị ngọt nhẹ.</li></ul><p>Tất cả nguyên liệu đều được chọn lọc từ những vườn trái cây sạch và chế biến ngay trong ngày để đảm bảo độ tươi ngon nhất.</p>',
    'https://images.unsplash.com/photo-1551198297-094dd136d3e9?q=80&w=800&auto=format&fit=crop',
    'PUBLISHED', 270, FALSE, 'review,summer,drinks',
    'Menu nước ép giải nhiệt mùa hè 2026', 'Review 3 loại đồ uống Tropical mới nhất tại nhà hàng giúp giải nhiệt mùa hè hiệu quả.',
    '2025-06-15 15:00:00', 56, 2
),
-- 10. Chatbot Guide
(
    'Hỏi đáp nhanh cùng Chatbot AI thông minh trên website của chúng tôi',
    'hoi-dap-nhanh-cung-chatbot-ai-thong-minh',
    'Khám phá cách sử dụng Chatbot tích hợp OpenAI để tra cứu menu và đặt bàn chỉ trong vài giây.',
    '<h2>Công nghệ AI đồng hành cùng thực khách</h2><p>Để mang lại sự tiện lợi tối đa, chúng tôi đã tích hợp <strong>Chatbot AI thông minh</strong> (sử dụng công nghệ từ OpenAI) ngay trên website. Bạn không còn phải chờ đợi nhân viên trực hotline nữa!</p><h3>Chatbot có thể giúp gì cho bạn?</h3><ul><li><strong>Tra cứu menu:</strong> Chỉ cần gõ "Thực đơn hôm nay có gì?", AI sẽ liệt kê các món đặc sắc nhất.</li><li><strong>Tư vấn món ăn:</strong> Bạn bị dị ứng với lạc (đậu phộng)? Hãy hỏi AI và nó sẽ lọc ra những món an toàn cho bạn.</li><li><strong>Kiểm tra tình trạng bàn:</strong> Hỏi về bàn trống vào giờ cao điểm một cách nhanh chóng.</li></ul><p>Hãy thử nhấn vào biểu tượng bong bóng chat màu xanh ở góc phải màn hình để bắt đầu trải nghiệm sự chuyên nghiệp từ công nghệ của chúng tôi!</p>',
    'https://plus.unsplash.com/premium_photo-1684761949804-fd8eb9a5b6cc?q=80&w=800&auto=format&fit=crop',
    'PUBLISHED', 600, FALSE, 'tutorial,ai,chatbot',
    'Cách sử dụng Chatbot AI thông minh đặt món', 'Trải nghiệm hỗ trợ khách hàng 24/7 bằng công nghệ AI tiên tiến nhất trên website nhà hàng.' ,
    '2026-01-22 14:00:00', 56, 4
);