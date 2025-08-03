# 🎫 COUPON MANAGEMENT SYSTEM - API DOCUMENTATION

## 📋 Tổng quan hệ thống

Hệ thống Coupon Management được thiết kế theo mô hình của các platform lớn như Shopee, Grab, Tiki với đầy đủ tính năng:

### ✨ Tính năng chính
- ✅ **CRUD Coupon** với validation nghiệp vụ đầy đủ
- ✅ **Áp dụng coupon linh hoạt** (theo category, food, user)
- ✅ **Tracking lịch sử** sử dụng chi tiết
- ✅ **Auto-update status** (expired, used out)
- ✅ **Scheduled tasks** tự động
- ✅ **Admin dashboard** với analytics
- ✅ **Bulk operations** cho admin
- ✅ **Integration** với Order System

### 📊 Kiến trúc hệ thống
```
📁 Entities:
├── Coupon (main entity với 15+ fields)
├── CouponUsage (usage tracking)
├── CouponType (PUBLIC, PRIVATE, BIRTHDAY...)  
├── CouponStatus (ACTIVE, EXPIRED, INACTIVE...)
└── DiscountType (PERCENT, AMOUNT)

📁 APIs:
├── CouponController (20+ endpoints for users)
├── CouponAdminController (10+ admin endpoints)
└── CouponIntegrationService (internal APIs)

📁 Services:
├── CouponService (60+ business methods)
├── CouponServiceImpl (500+ lines logic)
└── CouponScheduler (auto tasks)
```

---

## 🚀 API ENDPOINTS GUIDE

### **USER APIs** (Public/Customer facing)

#### 1. Lấy danh sách coupon public
```http
GET /api/v1/coupons/public/active
Response: List<CouponResponse>
```

#### 2. Lấy coupon available cho user
```http
GET /api/v1/coupons/user/{userId}/available
Response: List<CouponResponse>
```

#### 3. Validate coupon trước khi apply
```http
POST /api/v1/coupons/validate
Content-Type: application/json

{
  "couponCode": "SAVE20",
  "userId": 1,
  "orderAmount": 150000.0,
  "foodIds": [1, 2, 3]
}

Response: CouponApplyResult
{
  "success": true,
  "message": "Coupon applied successfully",
  "couponCode": "SAVE20",
  "originalAmount": 150000.0,
  "discountAmount": 30000.0,
  "finalAmount": 120000.0,
  "savedAmount": 30000.0
}
```

#### 4. Apply coupon vào đơn hàng
```http
POST /api/v1/coupons/apply
Content-Type: application/json

{
  "couponCode": "SAVE20",
  "userId": 1,
  "orderAmount": 150000.0,
  "foodIds": [1, 2, 3]
}
```

### **ADMIN APIs** (Management)

#### 1. Tạo coupon mới
```http
POST /api/v1/coupons
Content-Type: application/json

{
  "code": "FLASH50",
  "title": "Flash Sale 50%",
  "description": "Giảm 50% cho đơn từ 200k",
  "discountType": "PERCENT",
  "discountValue": 50.0,
  "maxDiscountAmount": 100000.0,
  "minOrderAmount": 200000.0,
  "maxUsagePerUser": 1,
  "startDate": "2024-08-02T00:00:00",
  "endDate": "2024-08-09T23:59:59",
  "maxUsage": 1000,
  "couponType": "PUBLIC",
  "applicableCategoryIds": [1, 2],
  "applicableFoodIds": [],
  "applicableUserIds": []
}
```

#### 2. Dashboard admin
```http
GET /api/v1/admin/coupons/dashboard
Response: {
  "statistics": {"ACTIVE": 5, "EXPIRED": 2},
  "mostUsedCoupons": [...],
  "expiringSoonCoupons": [...],
  "totalActiveCoupons": 5
}
```

#### 3. Analytics chi tiết
```http
GET /api/v1/admin/coupons/analytics
Response: {
  "totalCoupons": 10,
  "activeCoupons": 5,
  "activeRate": 50.0,
  "topPerformingCoupons": [...],
  "statusDistribution": {...}
}
```

#### 4. Bulk operations
```http
PUT /api/v1/admin/coupons/bulk-activate
Content-Type: application/json
[1, 2, 3, 4, 5]

PUT /api/v1/admin/coupons/bulk-deactivate
DELETE /api/v1/admin/coupons/bulk-delete
```

### **INTERNAL APIs** (Service Integration)

#### 1. Confirm coupon usage (từ Order Service)
```http
POST /api/v1/coupons/confirm-usage?couponCode=SAVE20&userId=1&orderId=123&discountAmount=30000
```

#### 2. Cancel coupon usage (khi order cancelled)
```http
DELETE /api/v1/coupons/usage/{usageId}
```

---

## 📅 SCHEDULED TASKS

Hệ thống tự động chạy các task sau:

| Task | Schedule | Mô tả |
|------|----------|-------|
| Update Expired | Daily 1:00 AM | Cập nhật coupon hết hạn |
| Update Used Out | Daily 2:00 AM | Cập nhật coupon hết lượt |
| Expiring Alert | Daily 9:00 AM | Thông báo coupon sắp hết hạn |
| Weekly Report | Sunday 11:00 PM | Báo cáo thống kê tuần |

---

## 🎯 BUSINESS LOGIC

### **Điều kiện áp dụng Coupon:**
1. ✅ Coupon status = ACTIVE
2. ✅ Trong thời hạn hiệu lực (startDate < now < endDate)
3. ✅ Chưa hết lượt sử dụng (usedCount < maxUsage)
4. ✅ User chưa dùng quá giới hạn (nếu có maxUsagePerUser)
5. ✅ Đơn hàng đủ giá trị tối thiểu (minOrderAmount)
6. ✅ Có món ăn/category phù hợp (nếu có ràng buộc)
7. ✅ User được phép dùng (cho PRIVATE coupon)

### **Tính toán giảm giá:**
```java
// PERCENT: discount = orderAmount * (discountValue / 100)
// Nhưng không vượt quá maxDiscountAmount

// AMOUNT: discount = discountValue
// Nhưng không vượt quá orderAmount
```

### **Workflow sử dụng:**
```
User input coupon → Validate → Calculate discount → Apply to order → 
Order success → Confirm usage → Update coupon stats
```

---

## 🔧 CONFIGURATION

### **Database Tables được tạo:**
- `coupons` (main table)
- `coupon_usage` (usage history)
- `coupon_categories` (many-to-many)
- `coupon_foods` (many-to-many)
- `coupon_users` (many-to-many)

### **Required Dependencies:**
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Validation
- Lombok
- Spring Boot Starter Scheduler

---

## 🎪 SAMPLE DATA

Xem file `demo-data.sql` để có data mẫu test hệ thống.

---

## 🚦 ERROR HANDLING

Hệ thống handle các lỗi phổ biến:
- ❌ Coupon not found
- ❌ Coupon expired/inactive
- ❌ User not eligible
- ❌ Order amount too low
- ❌ Coupon already used up
- ❌ Invalid food/category

---

## 📈 MONITORING & ANALYTICS

Admin có thể theo dõi:
- 📊 Thống kê coupon theo status
- 🏆 Top coupon được dùng nhiều nhất
- ⚠️ Coupon sắp hết hạn
- 💰 Tổng tiền đã giảm giá
- 👥 Số user đã sử dụng

---

Hệ thống này đã sẵn sàng production với đầy đủ tính năng như các platform thương mại điện tử lớn! 🚀
