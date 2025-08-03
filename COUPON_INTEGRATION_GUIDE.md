# 🔧 HƯỚNG DẪN TÍCH HỢP COUPON VÀO HỆ THỐNG FOOD ORDER

## 📋 Tổng quan những gì đã bổ sung

Để hệ thống Coupon hoạt động đầy đủ với hệ thống hiện tại của bạn, mình đã tạo thêm:

### **1. Database Changes**
- ✅ **upgrade_coupon_system.sql** - Nâng cấp bảng coupons với 7 trường mới
- ✅ **add_coupon_fields_to_orders.sql** - Thêm trường coupon vào bảng orders

### **2. Order Integration**
- ✅ **Order Entity** - Bổ sung 3 trường: couponCode, couponDiscountAmount, originalAmount
- ✅ **OrderRequest DTO** - Thêm các trường coupon cho API
- ✅ **OrderCouponService** - Service tích hợp coupon với order
- ✅ **OrderCouponController** - REST API cho coupon trong order

### **3. Configuration**
- ✅ **application.yml** - Cấu hình scheduler và coupon system
- ✅ **SchedulingConfig.java** - Enable Spring scheduling

---

## 🚀 CÁC BƯỚC TRIỂN KHAI

### **Bước 1: Chạy Database Migration**
```bash
# 1. Chạy upgrade coupon system
mysql -u admin123 -p food_ordering_system < src/main/resources/db/migration/upgrade_coupon_system.sql

# 2. Chạy upgrade orders table  
mysql -u admin123 -p food_ordering_system < src/main/resources/db/migration/add_coupon_fields_to_orders.sql
```

### **Bước 2: Restart Application**
```bash
cd /Users/user/Documents/ODER_FOOD/backend
mvn spring-boot:run
```

### **Bước 3: Test API Coupons**

#### **3.1. Test lấy coupon public:**
```bash
curl -X GET "http://localhost:8081/api/v1/coupons/public/active"
```

#### **3.2. Test preview coupon trong order:**
```bash
curl -X POST "http://localhost:8081/api/v1/orders/coupon/preview" \
-H "Content-Type: application/json" \
-d '{
  "couponCode": "SAVE20",
  "userId": 1,
  "orderAmount": 150000.0,
  "items": [
    {"foodId": 1, "quantity": 2},
    {"foodId": 2, "quantity": 1}
  ]
}'
```

#### **3.3. Test tạo coupon mới (Admin):**
```bash
curl -X POST "http://localhost:8081/api/v1/coupons" \
-H "Content-Type: application/json" \
-d '{
  "code": "NEWUSER50",
  "title": "New User 50%",
  "description": "Giảm 50% cho người dùng mới",
  "discountType": "PERCENT",
  "discountValue": 50.0,
  "maxDiscountAmount": 100000.0,
  "minOrderAmount": 200000.0,
  "startDate": "2024-08-02T00:00:00",
  "endDate": "2024-12-31T23:59:59",
  "maxUsage": 1000,
  "couponType": "PUBLIC"
}'
```

---

## 🔗 WORKFLOW TÍCH HỢP VỚI ORDER SYSTEM

### **Frontend to Backend Flow:**

1. **User chọn coupon trong giỏ hàng:**
```
Frontend → GET /api/v1/coupons/user/{userId}/available
→ Hiển thị danh sách coupon available
```

2. **User apply coupon (preview):**
```
Frontend → POST /api/v1/orders/coupon/preview
{
  "couponCode": "SAVE20",
  "userId": 1,
  "orderAmount": 150000.0,
  "items": [...]
}
→ Trả về discount amount và final amount
```

3. **User xác nhận đặt hàng:**
```
Frontend → POST /api/orders (existing endpoint)
{
  "userId": 1,
  "couponCode": "SAVE20",  // ← NEW FIELD
  "originalAmount": 150000, // ← NEW FIELD  
  "totalPrice": 120000,     // Final amount after discount
  "items": [...]
}
```

4. **Backend tự động confirm coupon:**
```
OrderService.createOrder() → OrderCouponService.confirmCouponUsage()
→ Update coupon usage count và lưu lịch sử
```

### **Cập nhật OrderService hiện tại:**

Trong file **OrderServiceImpl.java** của bạn, thêm logic này vào method `createOrder()`:

```java
@Autowired
private OrderCouponService orderCouponService;

public OrderResponse createOrder(OrderRequest orderRequest) {
    // ...existing logic...
    
    // NEW: Xử lý coupon nếu có
    if (orderRequest.getCouponCode() != null) {
        // Convert OrderItemRequest to OrderItem for validation
        List<OrderItem> orderItems = orderRequest.getItems().stream()
            .map(this::convertToOrderItem)
            .collect(Collectors.toList());
            
        // Apply coupon
        CouponApplyResult couponResult = orderCouponService.applyCouponToOrder(
            orderRequest.getCouponCode(),
            orderRequest.getUserId(),
            orderRequest.getOriginalAmount().doubleValue(),
            orderItems
        );
        
        if (couponResult.getSuccess()) {
            // Update order with coupon info
            orderCouponService.updateOrderWithCouponInfo(order, couponResult);
        }
    }
    
    // Save order
    Order savedOrder = orderRepository.save(order);
    
    // NEW: Confirm coupon usage after order success
    if (savedOrder.getCouponCode() != null) {
        orderCouponService.confirmCouponUsage(savedOrder);
    }
    
    // ...existing logic...
    return orderResponse;
}
```

---

## 📊 SCHEDULED TASKS TỰ ĐỘNG

Hệ thống sẽ tự động chạy các task sau:

| Thời gian | Task | Mô tả |
|-----------|------|-------|
| **1:00 AM hàng ngày** | Update Expired Coupons | Tự động đánh dấu coupon hết hạn |
| **2:00 AM hàng ngày** | Update Used Out Coupons | Cập nhật coupon hết lượt sử dụng |
| **9:00 AM hàng ngày** | Expiring Alert | Thông báo coupon sắp hết hạn |
| **11:00 PM Chủ nhật** | Weekly Report | Báo cáo thống kê tuần |

---

## 🎯 API ENDPOINTS MỚI

### **User APIs:**
- `GET /api/v1/coupons/public/active` - Lấy coupon công khai
- `GET /api/v1/coupons/user/{userId}/available` - Coupon cho user
- `POST /api/v1/orders/coupon/preview` - Preview discount
- `GET /api/v1/orders/coupon/validate` - Validate nhanh coupon

### **Admin APIs:**
- `POST /api/v1/coupons` - Tạo coupon mới
- `GET /api/v1/admin/coupons/dashboard` - Dashboard admin
- `GET /api/v1/admin/coupons/analytics` - Phân tích hiệu quả
- `PUT /api/v1/admin/coupons/bulk-activate` - Kích hoạt hàng loạt

### **System APIs:**
- `POST /api/v1/coupons/confirm-usage` - Confirm sử dụng coupon
- `DELETE /api/v1/coupons/usage/{usageId}` - Hủy sử dụng coupon

---

## 🎪 DEMO DATA SẴN CÓ

Sau khi chạy migration, bạn sẽ có sẵn:
- ✅ **8 coupon mẫu** (PUBLIC, BIRTHDAY, FIRST_ORDER...)
- ✅ **Sample usage history** 
- ✅ **Liên kết với categories và foods**
- ✅ **Private coupon cho users**

---

## 🔧 TROUBLESHOOTING

### **Lỗi thường gặp:**

1. **"Table 'coupons' doesn't exist"**
   - Chạy lại file `upgrade_coupon_system.sql`

2. **"Column 'coupon_code' doesn't exist in 'orders'"**
   - Chạy file `add_coupon_fields_to_orders.sql`

3. **Scheduled tasks không chạy:**
   - Kiểm tra `@EnableScheduling` trong SchedulingConfig
   - Xem log: `logging.level.org.springframework.scheduling: DEBUG`

4. **Coupon không apply được:**
   - Kiểm tra user_id, food_id có tồn tại không
   - Xem log chi tiết trong CouponServiceImpl

---

## ✅ CHECKLIST HOÀN THÀNH

- [ ] Chạy 2 file migration SQL
- [ ] Restart application
- [ ] Test API `/api/v1/coupons/public/active`  
- [ ] Test preview coupon trong order
- [ ] Cập nhật OrderServiceImpl với coupon logic
- [ ] Test create order với coupon
- [ ] Kiểm tra scheduled tasks hoạt động
- [ ] Test admin dashboard

---

**🎉 Sau khi hoàn thành checklist, hệ thống Coupon sẽ hoạt động đầy đủ như các platform lớn!**

Nếu gặp vấn đề gì, hãy cho mình biết để support thêm! 🚀
