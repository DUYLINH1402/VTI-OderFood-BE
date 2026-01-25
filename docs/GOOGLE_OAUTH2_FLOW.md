# Google OAuth2 Authorization Code Flow - Hướng dẫn

> **Lưu ý**: Hệ thống cũng hỗ trợ [Facebook OAuth2](./FACEBOOK_OAUTH2_FLOW.md) với flow tương tự.

## Tổng quan Flow

```
User (Browser) → Backend → Google → Backend → Frontend
```

### Chi tiết các bước:

1. **User bấm Login với Google trên React**
   - React **KHÔNG** mở popup
   - React chuyển hướng toàn bộ trình duyệt đến: `{BACKEND_URL}/oauth2/authorization/google`
   ```javascript
   // Frontend code
   window.location.href = 'https://api.dongxanhfood.shop/oauth2/authorization/google';
   ```

2. **Backend nhận request và redirect đến Google**
   - Backend trả về HTTP 302 với Location header đến Google OAuth
   - Trình duyệt tự động tải trang đăng nhập Google (toàn màn hình)

3. **User đăng nhập trên Google**
   - User nhập email/password hoặc chọn tài khoản Google
   - Google xác thực user

4. **Google callback về Backend**
   - Google redirect user về: `{BACKEND_URL}/login/oauth2/code/google?code=xyz...`
   - Backend nhận authorization code và exchange lấy access_token/id_token với Google (bước này user không thấy)

5. **Backend xử lý và redirect về Frontend**
   - Backend tạo Custom JWT token
   - Backend redirect về: `{FRONTEND_URL}/login-success?token=JWT_TOKEN`
   - Nếu lỗi: `{FRONTEND_URL}/login-success?error=ERROR_CODE`

6. **Frontend nhận JWT token**
   - Frontend lấy token từ URL query parameter
   - Lưu token vào localStorage
   - Gọi `GET /api/users/profile` để lấy thông tin user đầy đủ
   - Redirect user về trang chính

## Cấu hình cần thiết

### 1. Biến môi trường (.env)

```env
# Google OAuth2
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

# Frontend redirect URL sau khi login thành công
OAUTH2_REDIRECT_URL=https://dongxanhfood.shop/login-success
```

### 2. Google Cloud Console

Vào [Google Cloud Console](https://console.cloud.google.com/):
1. Chọn project
2. Vào **APIs & Services** → **Credentials**
3. Trong OAuth 2.0 Client IDs, thêm **Authorized redirect URIs**:
   - `https://dongxanhfoodorder.shop/login/oauth2/code/google` (Production)
   - `http://localhost:8080/login/oauth2/code/google` (Development)

## Error Codes

| Error Code | Mô tả |
|------------|-------|
| `GOOGLE_EMAIL_NOT_VERIFIED` | Email Google chưa được xác minh |
| `USER_LOCKED` | Tài khoản đã bị khóa |
| `OAUTH2_LOGIN_FAILED` | Lỗi chung khi đăng nhập OAuth2 |
| `OAUTH2_ACCESS_DENIED` | User từ chối cấp quyền |
| `OAUTH2_INVALID_TOKEN` | Token không hợp lệ |
| `OAUTH2_TOKEN_EXPIRED` | Token đã hết hạn |

## Ưu điểm so với ID Token Flow (cũ)

1. **Bảo mật hơn**: Authorization code được xử lý ở backend, access token không bị lộ ở frontend
2. **Tuân thủ chuẩn**: OAuth 2.0 Authorization Code Flow là flow được khuyến nghị cho web apps
3. **Tích hợp sẵn**: Sử dụng Spring Security OAuth2 Client, không cần tự verify token
4. **Refresh token**: Có thể lấy refresh token để tự động gia hạn session (nếu cần)

## API Endpoints

| Endpoint | Method | Mô tả |
|----------|--------|-------|
| `/oauth2/authorization/google` | GET | Bắt đầu OAuth2 flow, redirect đến Google |
| `/login/oauth2/code/google` | GET | Callback từ Google (internal) |
| `/api/users/profile` | GET | Lấy thông tin user đầy đủ (cần Bearer token) |

### Flow mới (Authorization Code - Redirect)
```
1. FE: window.location.href = "/oauth2/authorization/google"
2. BE: Redirect 302 → Google Login Page
3. User: Đăng nhập trên Google (toàn màn hình)
4. Google: Callback → BE với authorization code
5. BE: Exchange code → Tạo JWT → Redirect FE với ?token=JWT
6. FE: Lấy token từ URL, lưu vào localStorage
7. FE: Gọi GET /api/users/profile với Authorization header
8. FE: Nhận { id, email, fullName, point, roleCode, ... }
9. FE: Lưu user info, redirect về trang chính
```

### Ưu điểm Flow mới
- **Bảo mật hơn**: Authorization code chỉ được xử lý ở backend
- **Không cần popup**: Tránh bị chặn bởi popup blocker
- **Chuẩn OAuth2**: Tuân thủ best practices
