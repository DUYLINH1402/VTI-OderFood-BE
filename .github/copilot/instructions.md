# Copilot Instructions for Food Order Backend (Java Spring Boot)

## Language Preference

Luôn phản hồi cho người dùng bằng Tiếng Việt trong mọi tình huống, bao gồm cả giải thích, ví dụ code, và hướng dẫn.

## Tổng quan dự án Backend

- Sử dụng Java Spring Boot để xây dựng RESTful API cho hệ thống đặt món ăn trực tuyến.
- Các chức năng chính:
  - Xác thực và phân quyền người dùng (khách, nhân viên, admin)
  - Quản lý thực đơn, món ăn
  - Quản lý giỏ hàng, đơn hàng, trạng thái đơn
  - Quản lý điểm thưởng, sử dụng điểm khi thanh toán
  - Quản lý người dùng, thống kê, báo cáo
  - Gửi email xác thực, thông báo trạng thái đơn

## Quy tắc code Backend (chi tiết từ toàn bộ dự án)

- Kiến trúc chuẩn Spring Boot: Tách rõ Controller, Service, Repository, Entity, DTO cho từng module (auth, cart, food, order, user, payments, zone, feedbacks, favorite...).
- Sử dụng annotation: @RestController, @Service, @Repository, @Entity, @RequestMapping, @Valid, @Autowired, @CrossOrigin, @Builder, @Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor, @PreAuthorize...
- Quản lý lỗi tập trung: Dùng GlobalExceptionHandler để xử lý và trả về lỗi chuẩn hóa (ApiError, BadRequestException, ResourceNotFoundException...).
- Đặt tên endpoint rõ ràng, tuân thủ RESTful: Ví dụ `/api/foods/new`, `/api/cart`, `/api/order`, `/api/user`, `/api/payments`.
- Validate dữ liệu đầu vào: Sử dụng annotation và custom validator (ví dụ: ValidPasswordValidator, @Valid cho DTO).
- Sử dụng DTO cho request/response: Tách biệt entity và dữ liệu truyền qua API, giúp bảo mật và dễ mở rộng.
- Comment cho logic phức tạp hoặc nghiệp vụ đặc thù: Đặc biệt ở các hàm xử lý nghiệp vụ, validator, exception.
- Quản lý phân quyền, xác thực: Sử dụng JWT, CustomUserDetails, @PreAuthorize, phân quyền cho các loại user (khách, nhân viên, admin).
- Tối ưu hiệu năng truy vấn DB: Sử dụng Pageable cho phân trang, query rõ ràng, repository riêng cho từng entity.
- Quản lý trạng thái đơn hàng, giỏ hàng, thực đơn, điểm thưởng: Tách service cho từng nghiệp vụ, entity rõ ràng cho từng loại dữ liệu.
- Sử dụng migration SQL cho thay đổi cấu trúc DB: Quản lý version, dễ bảo trì.
- Luôn trả về thông báo lỗi rõ ràng, dễ hiểu cho client: Thống nhất format lỗi, status code.
- Quy trình phát triển: Sử dụng branch cho từng tính năng, commit message rõ ràng, review code trước khi merge, viết test cho các service/controller quan trọng, dùng môi trường dev/test trước khi lên production.
- Lưu ý bảo mật: Luôn kiểm tra xác thực và phân quyền trước các thao tác nhạy cảm, sử dụng biến môi trường cho thông tin bảo mật, đảm bảo bảo mật dữ liệu người dùng.

## Cấu trúc thư mục Backend

- `src/main/java/com/foodorder/backend/`: Mã nguồn Java.
  - `auth/`: Xác thực, phân quyền người dùng.
  - `cart/`: Quản lý giỏ hàng.
  - `order/`: Quản lý đơn hàng.
  - `user/`: Quản lý thông tin người dùng.
  - ...
- `src/main/resources/`: Cấu hình, template email, migration SQL.

## Quy trình phát triển

- Sử dụng branch cho từng tính năng.
- Commit message rõ ràng, ngắn gọn, có ý nghĩa.
- Luôn review code trước khi merge.
- Viết test cho các service và controller quan trọng.
- Sử dụng môi trường dev/test trước khi lên production.

## Lưu ý đặc biệt

- Luôn kiểm tra xác thực và phân quyền trước các thao tác nhạy cảm.
- Sử dụng biến môi trường cho thông tin bảo mật.
- Tối ưu hiệu năng cho các truy vấn DB và API.
- Đảm bảo bảo mật dữ liệu người dùng.

## Hướng dẫn cho Copilot

- Khi sinh code, luôn tuân thủ các quy tắc trên.
- Giải thích bằng Tiếng Việt, ưu tiên ví dụ thực tế từ dự án.
- Nếu có logic phức tạp, hãy comment rõ ràng.
- Khi được hỏi về cấu trúc, hãy trả lời dựa trên các mục ở trên.
