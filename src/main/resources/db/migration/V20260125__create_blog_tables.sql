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
    'https://images.unsplash.com/photo-1551024709-8f23befc6f87?q=80&w=800&auto=format&fit=crop',
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
    '<h2>Công nghệ AI đồng hành cùng thực khách</h2><p>Để mang lại sự tiện lợi tối đa, chúng tôi đã tích hợp <strong>Chatbot AI thông minh</strong> (sử dụng công nghệ từ OpenAI) ngay trên website. Bạn không còn phải chờ đợi nhân viên trực hotline nữa!</p><h3>Chatbot có thể giúp gì cho bạn?</h3><ul><li><strong>Tra cứu menu:</strong> Chỉ cần gõ "Thực đơn hôm nay có gì?", AI sẽ liệt kê các món đặc sắc nhất.</li><li><strong>Tư vấn món ăn:</strong> Bạn bị dị ứng với lạc (đậu phộng)? Hãy hỏi AI và nó sẽ lọc ra những món an toàn cho bạn.</li><li><strong>Kiểm tra tình trạng bàn:</strong> Hỏi về bàn trống vào giờ cao điểm một cách nhanh chóng.</li></ul><p>Hãy thử nhấn vào biểu Robot chat màu xanh lá ở góc phải màn hình để bắt đầu trải nghiệm sự chuyên nghiệp từ công nghệ của chúng tôi!</p>',
    'https://plus.unsplash.com/premium_photo-1684761949804-fd8eb9a5b6cc?q=80&w=800&auto=format&fit=crop',
    'PUBLISHED', 600, FALSE, 'tutorial,ai,chatbot',
    'Cách sử dụng Chatbot AI thông minh đặt món', 'Trải nghiệm hỗ trợ khách hàng 24/7 bằng công nghệ AI tiên tiến nhất trên website nhà hàng.' ,
    '2026-01-22 14:00:00', 56, 4
),
-- 11. Membership Program (Promotion)
(
    'Thẻ thành viên Privilege: Càng ăn nhiều, ưu đãi càng lớn',
    'the-thanh-vien-privilege-uu-dai-lon',
    'Chương trình tích điểm đổi quà và giảm giá trực tiếp cho khách hàng thân thiết của hệ thống trong năm 2026.',
    '<h2>Gia nhập cộng đồng Foodie Privilege</h2><p>Chúng tôi chính thức ra mắt hệ thống <strong>Thẻ thành viên điện tử</strong> tích hợp ngay trên tài khoản website của bạn. Giờ đây, mỗi hóa đơn thanh toán đều mang lại cho bạn những giá trị vượt trội.</p><h3>Các hạng thẻ và đặc quyền:</h3><ul><li><strong>Hạng Bạc (Silver):</strong> Giảm 5% cho mọi hóa đơn, tặng voucher sinh nhật 100k.</li><li><strong>Hạng Vàng (Gold):</strong> Giảm 10% hóa đơn, ưu tiên đặt bàn vào giờ cao điểm, miễn phí giao hàng bán kính 5km.</li><li><strong>Hạng Kim Cương (Diamond):</strong> Giảm 15%, thưởng thức các món mới trong menu thử nghiệm miễn phí, có quản lý riêng hỗ trợ đặt tiệc.</li></ul><p>Chỉ cần đăng ký tài khoản và thực hiện đơn hàng đầu tiên để bắt đầu tích lũy điểm ngay hôm nay!</p>',
    'https://images.unsplash.com/photo-1556742044-3c52d6e88c62?q=80&w=800&auto=format&fit=crop',
    'PUBLISHED', 320, TRUE, 'promotion,membership,loyalty',
    'Chương trình khách hàng thân thiết Privilege 2026', 'Tích điểm đổi quà và nhận ưu đãi giảm giá lên đến 15% cho mỗi đơn hàng tại nhà hàng.',
    '2026-02-05 10:00:00', 56, 1
),
-- 12. Seafood Review (Review)
(
    'Hương vị đại dương: Thưởng thức tháp hải sản khổng lồ "Seafood Tower"',
    'thuong-thuc-thap-hai-san-khong-lo',
    'Đắm chìm trong sự tươi ngon của tôm hùm, cua hoàng đế và hàu tươi được nhập khẩu trực tiếp trong ngày.',
    '<h2>Trải nghiệm thượng lưu với Seafood Tower</h2><p>Nếu bạn là một tín đồ của hải sản, tháp hải sản 3 tầng tại nhà hàng là một lựa chọn không thể bỏ qua. Mỗi tầng là một bất ngờ về hương vị và sự tươi mới.</p><h3>Cấu trúc tháp hải sản:</h3><ul><li><strong>Tầng 1:</strong> Hàu Miyagi Nhật Bản ăn kèm sốt Tabasco và chanh vàng.</li><li><strong>Tầng 2:</strong> Tôm hùm Alaska hấp rượu vang trắng và tôm sú biển nướng muối ớt.</li><li><strong>Tầng 3:</strong> Cua Tuyết sốt Cajun cay nồng và các loại ốc biển cao cấp.</li></ul><p>Chúng tôi cam kết 100% hải sản được bảo quản trong bể kính và chỉ chế biến khi có yêu cầu từ khách hàng để giữ trọn vị ngọt tự nhiên của biển cả.</p>',
    'https://images.unsplash.com/photo-1551489186-cf8726f514f8?q=80&w=800&auto=format&fit=crop',
    'PUBLISHED', 680, FALSE, 'review,seafood,fine-dining',
    'Review Tháp Hải Sản khổng lồ tại nhà hàng', 'Khám phá hương vị tươi ngon của tôm hùm và cua hoàng đế trong thực đơn hải sản mới nhất.',
    '2026-02-10 11:30:00', 56, 2
),
-- 13. New Branch (Internal News)
(
    'Mở rộng bản đồ ẩm thực: Khai trương chi nhánh thứ 3 tại trung tâm Quận 1',
    'khai-truong-chi-nhanh-thu-3-quan-1',
    'Tiếp nối sự thành công, chi nhánh mới với không gian sang trọng và view triệu đô sẽ chính thức đi vào hoạt động từ tháng 3/2026.',
    '<p>Sau hơn 2 năm phục vụ thực khách tại hai chi nhánh hiện có, chúng tôi vô cùng hào hứng thông báo về việc khai trương <strong>Chi nhánh thứ 3</strong> tại địa chỉ: 123 Lê Lợi, Quận 1, TP.HCM.</p><h3>Không gian đậm chất nghệ thuật</h3><p>Chi nhánh mới được thiết kế theo phong cách Indochine kết hợp hiện đại, với không gian mở và hệ thống kính tràn viền giúp thực khách có thể ngắm nhìn toàn cảnh thành phố về đêm.</p><h3>Tuần lễ khai trương vàng</h3><p>Từ ngày 01/03 đến 07/03, chi nhánh Quận 1 sẽ áp dụng chương trình <strong>"Đi 4 tính tiền 3"</strong> cho tất cả các set menu và tặng kèm 01 phần quà lưu niệm cho 50 khách hàng đầu tiên mỗi ngày.</p>',
    'https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?q=80&w=800&auto=format&fit=crop',
    'PUBLISHED', 1100, TRUE, 'news,internal,grand-opening',
    'Khai trương chi nhánh nhà hàng mới tại Quận 1', 'Chào đón chi nhánh thứ 3 với ưu đãi cực lớn và không gian sang trọng bậc nhất Sài Thành.',
    '2026-02-15 08:00:00', 56, 3
),
-- 14. WebSocket Tracking Guide (Tutorial)
(
    'Theo dõi đơn hàng thời gian thực: Không còn nỗi lo chờ đợi',
    'theo-doi-don-hang- thời-gian-thuc',
    'Hướng dẫn cách sử dụng tính năng thông báo trực tiếp trên website để biết chính xác món ăn của bạn đang ở giai đoạn nào.',
    '<h2>Công nghệ kết nối bếp và thực khách</h2><p>Nhằm giảm bớt sự lo lắng khi đặt hàng online, hệ thống của chúng tôi đã tích hợp công nghệ <strong>Websocket</strong>, cho phép cập nhật trạng thái đơn hàng ngay lập tức mà không cần tải lại trang.</p><h3>Các trạng thái đơn hàng bạn sẽ nhận được:</h3><ul><li><strong>Đã xác nhận:</strong> Nhà hàng đã nhận đơn và bắt đầu chuẩn bị.</li><li><strong>Đang chế biến:</strong> Đầu bếp đang thực hiện món ăn của bạn.</li><li><strong>Đang giao hàng:</strong> Shipper đã lấy hàng và đang trên đường đến địa chỉ của bạn.</li><li><strong>Hoàn thành:</strong> Đơn hàng đã được giao thành công.</li></ul><p>Bạn cũng có thể nhắn tin trực tiếp với nhân viên hỗ trợ thông qua khung chat ở góc màn hình nếu có bất kỳ thay đổi nào về đơn hàng.</p>',
    'https://plus.unsplash.com/premium_vector-1727150463713-451b8d5e90e9?q=80&w=800&auto=format&fit=crop',
    'PUBLISHED', 430, FALSE, 'tutorial,websocket,order-tracking',
    'Cách theo dõi đơn hàng online thời gian thực', 'Trải nghiệm tính năng cập nhật trạng thái đơn hàng tự động thông qua công nghệ Websocket hiện đại.',
    '2026-01-28 09:00:00', 56, 4
),
-- 15. Weekend Brunch (Promotion)
(
    'Weekend Brunch: Nạp năng lượng cho ngày cuối tuần thảnh thơi',
    'weekend-brunch-nap-nang-luong-cuoi-tuan',
    'Tận hưởng bữa sáng muộn đầy dưỡng chất với thực đơn buffet nhẹ nhàng và cà phê Specialty miễn phí.',
    '<h2>Khi bữa sáng gặp gỡ bữa trưa</h2><p>Chương trình <strong>Weekend Brunch</strong> là khoảng thời gian lý tưởng để bạn cùng gia đình thư giãn sau một tuần làm việc căng thẳng. Không cần dậy quá sớm, bạn vẫn có thể thưởng thức những món ăn tinh tế nhất.</p><h3>Thực đơn Brunch đặc sắc:</h3><ul><li>Các loại bánh mì Artisan nướng giòn kèm bơ thảo mộc.</li><li>Trứng Benedict sốt Hollandaise mịn màng.</li><li>Quầy Salad bar với hơn 15 loại sốt tự chọn.</li><li>Cà phê pha máy chuẩn Ý hoặc nước ép trái cây tươi.</li></ul><p>Thời gian: 10:00 - 14:00 mỗi Thứ Bảy và Chủ Nhật hàng tuần.</p>',
    'https://images.unsplash.com/photo-1467003909585-2f8a72700288?q=80&w=800&auto=format&fit=crop',
    'PUBLISHED', 285, FALSE, 'promotion,brunch,weekend',
    'Thực đơn Weekend Brunch cuối tuần hấp dẫn', 'Tận hưởng bữa sáng muộn sang trọng cùng gia đình với menu Buffet Brunch đa dạng món Âu - Á.',
    '2026-02-12 15:00:00', 56, 1
),
-- 16. Dessert Review (Review)
(
    'Nghệ thuật đồ ngọt: Khi món tráng miệng là một bức tranh',
    'nghe-thuat-do-ngot-mon-trang-mieng',
    'Khám phá bộ sưu tập đồ ngọt mới nhất, nơi hương vị hòa quyện cùng sự tinh tế trong cách trình bày.',
    '<h2>Vị ngọt kết thúc một bữa tiệc hoàn hảo</h2><p>Tại nhà hàng, món tráng miệng không chỉ là món ăn thêm, đó là một tác phẩm nghệ thuật. Đầu bếp bánh của chúng tôi luôn tìm cách cân bằng giữa vị ngọt, độ béo và màu sắc tự nhiên.</p><h3>Điểm nhấn trong menu đồ ngọt:</h3><ul><li><strong>Tiramisu chậu cây:</strong> Một sự sáng tạo thú vị với vị đắng của cà phê và lớp kem Mascarpone mềm mịn, trình bày như một chậu cây nhỏ xinh.</li><li><strong>Mousse xoài cốt dừa:</strong> Sử dụng xoài cát Hòa Lộc chín mọng, mang lại vị chua thanh mát đặc trưng của vùng nhiệt đới.</li><li><strong>Bánh Macaron đủ vị:</strong> Sự tinh tế từ nước Pháp với lớp vỏ giòn tan và nhân kem đa dạng.</li></ul><p>Hãy để chúng tôi làm ngọt ngào thêm câu chuyện của bạn bằng những món quà từ thiên nhiên này.</p>',
    'https://images.unsplash.com/photo-1551024506-0bccd828d307?q=80&w=800&auto=format&fit=crop',
    'PUBLISHED', 510, TRUE, 'review,dessert,cake',
    'Review các món tráng miệng cao cấp tại nhà hàng', 'Thưởng thức Tiramisu, Mousse xoài và nghệ thuật làm bánh đỉnh cao từ các đầu bếp chuyên nghiệp.',
    '2026-02-18 14:00:00', 56, 2
),
-- 17. Farm to Table (Internal News)
(
    'Từ nông trại đến bàn ăn: Hành trình của nguyên liệu Organic',
    'tu-nong-trai-den-ban-an-nguyen-lieu-organic',
    'Khám phá quy trình tuyển chọn nguyên liệu nghiêm ngặt từ các trang trại VietGAP để đảm bảo sức khỏe cho thực khách.',
    '<h2>Sức khỏe của bạn là ưu tiên hàng đầu</h2><p>Trong năm 2026, chúng tôi đẩy mạnh chiến dịch <strong>"Green Table"</strong>. Hơn 80% rau củ và thịt tại nhà hàng hiện nay được cung cấp trực tiếp từ các nông trại hữu cơ tại Đà Lạt và các tỉnh miền Tây.</p><h3>Cam kết 3 Không:</h3><ol><li>Không sử dụng chất bảo quản thực phẩm.</li><li>Không dùng rau củ biến đổi gen (Non-GMO).</li><li>Không sử dụng thuốc trừ sâu hóa học trong quá trình canh tác.</li></ol><p>Mỗi món ăn bạn thưởng thức không chỉ ngon mà còn là sự an tâm tuyệt đối về nguồn gốc và chất lượng dinh dưỡng.</p>',
    'https://images.unsplash.com/photo-1464226184884-fa280b87c399?q=80&w=800&auto=format&fit=crop',
    'PUBLISHED', 390, FALSE, 'news,organic,health',
    'Nguồn nguyên liệu sạch Organic tại nhà hàng', 'Hành trình mang thực phẩm sạch từ nông trại Đà Lạt đến bàn ăn của bạn với tiêu chuẩn VietGAP.',
    '2026-01-25 08:30:00', 56, 3
),
-- 18. VIP Room Booking (Tutorial)
(
    'Đặt phòng VIP riêng tư: Lựa chọn hoàn hảo cho đối tác và gia đình',
    'dat-phong-vip-rieng-tu-cho-doi-tac',
    'Hướng dẫn đặt phòng riêng trên website để có không gian yên tĩnh cho các cuộc họp quan trọng hoặc tiệc gia đình.',
    '<h2>Không gian riêng tư giữa lòng thành phố</h2><p>Bạn cần một nơi yên tĩnh để bàn công việc hay muốn một không gian riêng tư cho lễ kỷ niệm gia đình? Hệ thống phòng VIP của chúng tôi sẽ đáp ứng mọi yêu cầu của bạn.</p><h3>Tiện ích tại phòng VIP:</h3><ul><li>Cách âm tuyệt đối, hệ thống điều hòa riêng biệt.</li><li>Hỗ trợ màn hình máy chiếu và âm thanh cho các cuộc họp.</li><li>Menu phục vụ riêng tại bàn theo yêu cầu của gia chủ.</li></ul><p><strong>Cách đặt:</strong> Tại trang chủ, chọn mục "Đặt bàn", sau đó tích vào tùy chọn "Phòng riêng". Chatbot AI của chúng tôi cũng có thể giúp bạn chọn phòng phù hợp với số lượng khách.</p>',
    'https://images.unsplash.com/photo-1514362545857-3bc16c4c7d1b?q=80&w=800&auto=format&fit=crop',
    'PUBLISHED', 150, FALSE, 'tutorial,vip-room,booking',
    'Hướng dẫn đặt phòng VIP riêng tư online', 'Dịch vụ đặt phòng riêng cho hội họp và tiệc gia đình với không gian sang trọng, yên tĩnh.',
    '2026-02-20 10:00:00', 56, 4
),
-- 19. Family Combo (Promotion)
(
    'Tiệc gia đình ấm cúng với Combo "Home Sweet Home"',
    'tiec-gia-dinh-combo-home-sweet-home',
    'Gói thực đơn tiết kiệm dành cho nhóm từ 4-6 người với đầy đủ các món đặc sắc nhất của nhà hàng.',
    '<h2>Gắn kết tình thân qua bữa ăn ngon</h2><p>Không gì hạnh phúc bằng việc cả gia đình quây quần bên mâm cơm ấm nóng. Combo <strong>"Home Sweet Home"</strong> được thiết kế để mang lại sự cân bằng dinh dưỡng và niềm vui cho mọi thành viên.</p><h3>Chi tiết Combo (Giá chỉ 1.299.000đ):</h3><ul><li>01 Salad ức gà nướng sốt mè rang.</li><li>01 Pizza hải sản size lớn hoặc 01 thố cơm chiên hải sản.</li><li>01 Sườn nướng BBQ tảng lớn.</li><li>01 Lẩu nấm sâm cầm thanh mát.</li><li>Miễn phí 01 bình nước ép trái cây theo mùa.</li></ul><p>Áp dụng cho cả ăn tại chỗ và đặt giao hàng về nhà.</p>',
    'https://images.unsplash.com/photo-1547573854-74d2a7ad4484?q=80&w=800&auto=format&fit=crop',
    'PUBLISHED', 470, FALSE, 'promotion,family-combo,dinner',
    'Set menu gia đình Home Sweet Home giá tốt', 'Ưu đãi đặt combo tiệc gia đình tiết kiệm 20% so với gọi món lẻ, phục vụ cho nhóm 4-6 người.',
    '2026-02-15 17:00:00', 56, 1
),
-- 20. Wine Pairing (Review)
(
    'Rượu vang và món ăn: Nghệ thuật kết hợp nâng tầm vị giác',
    'nghe-thuat-ket-hop-ruou-vang-mon-an',
    'Cùng chuyên gia Sommelier của nhà hàng khám phá cách chọn loại rượu vang phù hợp với từng loại thực phẩm.',
    '<h2>Sự hòa quyện hoàn hảo</h2><p>Một ly rượu vang đúng điệu có thể làm bừng sáng hương vị của món ăn. Tuy nhiên, không phải ai cũng biết quy tắc kết hợp sao cho chuẩn xác nhất.</p><h3>Gợi ý từ chuyên gia:</h3><ul><li><strong>Vang trắng:</strong> Rất hợp với hải sản, cá và các loại thịt trắng như gà nhờ độ axit cao giúp khử mùi tanh.</li><li><strong>Vang đỏ:</strong> Là người bạn đồng hành của Steak, thịt bò và các món có nhiều gia vị nhờ hàm lượng Tanin giúp trung hòa chất béo.</li><li><strong>Vang hồng/Vang nổ:</strong> Tuyệt vời cho các món khai vị và tráng miệng nhẹ nhàng.</li></ul><p>Hãy yêu cầu nhân viên phục vụ tư vấn loại vang phù hợp nhất cho món chính của bạn để có một trải nghiệm ẩm thực trọn vẹn nhất.</p>',
    'https://images.unsplash.com/photo-1510812431401-41d2bd2722f3?q=80&w=800&auto=format&fit=crop',
    'PUBLISHED', 310, FALSE, 'review,wine-pairing,sommelier',
    'Bí quyết kết hợp rượu vang và món ăn chuẩn vị', 'Hướng dẫn chọn rượu vang trắng, đỏ và vang nổ phù hợp với từng món ăn trong menu nhà hàng.' ,
    '2026-02-22 19:00:00', 56, 2
);