# Copilot Instructions - Food Order Backend (Spring Boot)

## Ngôn ngữ
**Luôn phản hồi bằng Tiếng Việt** trong mọi tình huống.

---

## Tổng quan dự án
- **Tech**: Java Spring Boot, Redis Cache, MySQL, JWT Auth
- **Chức năng**: Xác thực/phân quyền, quản lý món ăn, giỏ hàng, đơn hàng, điểm thưởng, thống kê

---

## Kiến trúc Spring Boot

| Layer | Mô tả | Ví dụ |
|-------|-------|-------|
| **Controller** | Xử lý request/response, định nghĩa endpoint | `FoodController` |
| **Service** | Interface định nghĩa nghiệp vụ | `FoodService` |
| **ServiceImpl** | Triển khai logic nghiệp vụ | `FoodServiceImpl` |
| **Repository** | Tương tác DB, truy vấn dữ liệu | `FoodRepository` |
| **Entity** | Ánh xạ bảng DB | `Food` |
| **DTO** | Request/Response object | `FoodRequest`, `FoodResponse` |

---

## Quy tắc code

### Cấu trúc & Convention
- Import đặt ở **đầu file**
- Endpoint RESTful: `/api/foods`, `/api/cart`, `/api/orders`
- Sử dụng `@Valid` cho validation DTO
- Comment rõ ràng cho logic phức tạp
- Phân quyền: `@PreAuthorize`, `@RequireStaff`, `@RequireAdmin`

### Error Handling
- Sử dụng `GlobalExceptionHandler`
- Trả về **errorCode chuẩn hóa**: `FOOD_NOT_FOUND`, `INVALID_CREDENTIALS`, `EMAIL_NOT_VERIFIED`
- Không trả message tự do, FE dựa vào errorCode để hiển thị

### Bảo mật
- Không tự ý sửa file `.env`
- Kiểm tra xác thực/phân quyền trước thao tác nhạy cảm
- Sử dụng biến môi trường cho thông tin bảo mật

---

## 🔴 QUAN TRỌNG: Cache với Redis

### Khi nào cần Cache?
| Loại API | Cần Cache? | TTL đề xuất |
|----------|------------|-------------|
| GET danh sách public (foods, blogs) | ✅ Có | 5 phút |
| GET chi tiết (food detail, blog detail) | ✅ Có | 5 phút |
| GET thống kê dashboard | ✅ Có | 10-15 phút |
| GET danh mục, config ít thay đổi | ✅ Có | 30 phút |
| GET comments, tương tác nhiều | ✅ Có | 3 phút |
| POST/PUT/DELETE | ❌ Không cache | - |

### TTL (Time To Live) Guidelines
```
TTL_SHORT = 3 phút    → Dữ liệu thay đổi thường xuyên (comments)
TTL_DEFAULT = 5 phút  → Dữ liệu chi tiết, danh sách
TTL_MEDIUM = 10 phút  → Danh sách admin, thống kê
TTL_LONG = 15 phút    → Dashboard, reports
TTL_VERY_LONG = 30 phút → Danh mục, config ít thay đổi
```

### Cách triển khai Cache

**1. Thêm cache constant vào `CacheConfig.java`:**
```java
public static final String MY_CACHE = "myCache";
// Thêm vào cacheConfigurations:
cacheConfigurations.put(MY_CACHE, defaultConfig.entryTtl(TTL_DEFAULT));
```

**2. Thêm @Cacheable cho GET methods:**
```java
@Cacheable(value = CacheConfig.MY_CACHE, key = "#id")
public MyResponse getById(Long id) { ... }

// Với phân trang:
@Cacheable(value = CacheConfig.MY_CACHE, 
           key = "#pageable.pageNumber + '_' + #pageable.pageSize")
public Page<MyResponse> getAll(Pageable pageable) { ... }
```

**3. Thêm @CacheEvict cho CUD methods:**
```java
@Caching(evict = {
    @CacheEvict(value = CacheConfig.MY_CACHE, allEntries = true),
    @CacheEvict(value = CacheConfig.MY_DETAIL_CACHE, allEntries = true)
})
public MyResponse create(MyRequest request) { ... }
```

**4. DTO phải implement Serializable:**
```java
public class MyResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    // fields...
}
```

---

## Tài liệu
- **Mỗi lần thêm/sửa chức năng** → Cập nhật hoặc tạo file `.md` trong `/docs`

---

## Checklist khi tạo API mới

- [ ] Tách đúng Controller → Service → ServiceImpl → Repository
- [ ] Sử dụng DTO cho request/response (implement Serializable nếu cần cache)
- [ ] Validate với `@Valid`
- [ ] Phân quyền phù hợp (`@RequireStaff`, `@RequireAdmin`)
- [ ] Error trả về errorCode chuẩn
- [ ] **Xem xét thêm Cache** cho GET APIs
- [ ] Cập nhật tài liệu trong `/docs`
