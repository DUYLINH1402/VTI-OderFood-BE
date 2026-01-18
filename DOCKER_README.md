# ===========================================
# Hướng dẫn sử dụng Docker cho Food Order System
# ===========================================

## 📋 Yêu cầu

- Docker Engine >= 20.10
- Docker Compose >= 2.0
- Ít nhất 4GB RAM khả dụng

## 🚀 Khởi động nhanh

### 1. Chuẩn bị file môi trường

# Mở file .env và điền các giá trị thực
nano .env  # hoặc vim, code...
```

### 2. Khởi động toàn bộ hệ thống

```bash
# Build và khởi động tất cả services
docker-compose up -d --build

# Xem logs
docker-compose logs -f

# Chỉ xem logs của backend
docker-compose logs -f backend
```

### 3. Kiểm tra trạng thái

```bash
# Xem trạng thái các container
docker-compose ps

# Kiểm tra health của backend
curl http://localhost:8081/actuator/health
```

## 📁 Cấu trúc Docker

```
backend/
├── Dockerfile                    # Build image cho backend
├── docker-compose.yml            # Orchestration tất cả services
├── .dockerignore                 # Files bỏ qua khi build
├── .env.docker.example           # Mẫu biến môi trường
├── docker/
│   └── mysql/
│       └── init/
│           └── 01-init.sql       # Script khởi tạo DB
└── src/main/resources/
    └── application-docker.yml    # Config cho Docker profile
```

## 🔧 Các lệnh Docker hữu ích

### Quản lý containers

```bash
# Khởi động
docker-compose up -d

# Dừng
docker-compose stop

# Dừng và xóa containers
docker-compose down

# Dừng, xóa containers và volumes (XÓA DỮ LIỆU!)
docker-compose down -v

# Restart một service
docker-compose restart backend
```

### Xem logs

```bash
# Xem tất cả logs
docker-compose logs

# Xem logs real-time
docker-compose logs -f

# Xem logs của service cụ thể
docker-compose logs -f backend
docker-compose logs -f mysql
```

### Truy cập container

```bash
# Truy cập MySQL
docker-compose exec mysql mysql -u foodorder -p food_ordering_system

# Truy cập shell của backend
docker-compose exec backend sh
```

### Build lại

```bash
# Build lại không dùng cache
docker-compose build --no-cache

# Build và khởi động lại
docker-compose up -d --build
```

## 🌐 Endpoints

| Service  | URL                              | Mô tả           |
|----------|----------------------------------|-----------------|
| Backend  | http://localhost:8081            | Spring Boot API |
| MySQL    | localhost:3306                   | Database        |
| Redis    | localhost:6379                   | Cache           |

## 🔒 Bảo mật

1. **KHÔNG** commit file `.env` lên Git
2. Thay đổi mật khẩu mặc định trong production
3. Sử dụng secrets manager cho production (Docker Secrets, Vault...)
4. Đảm bảo firewall chỉ mở ports cần thiết

## 🐛 Troubleshooting

### Backend không kết nối được MySQL

```bash
# Kiểm tra MySQL đã ready chưa
docker-compose logs mysql

# Đợi MySQL khởi động hoàn toàn (khoảng 30s)
docker-compose restart backend
```

### Lỗi "port already in use"

```bash
# Tìm process đang dùng port
lsof -i :8081
lsof -i :3306

# Kill process hoặc đổi port trong docker-compose.yml
```

### Xóa sạch và bắt đầu lại

```bash
# Xóa tất cả containers, volumes, networks
docker-compose down -v --remove-orphans
docker system prune -f

# Khởi động lại
docker-compose up -d --build
```

## 📊 Monitoring

### Kiểm tra resources

```bash
# Xem CPU/Memory usage
docker stats

# Xem disk usage
docker system df
```

### Health checks

```bash
# Backend health
curl http://localhost:8081/actuator/health

# MySQL health
docker-compose exec mysql mysqladmin ping -h localhost -u root -p
```

## 🔄 Development với Docker

### Chạy chỉ Database (dev locally)

```bash
# Chỉ chạy MySQL và Redis
docker-compose up -d mysql redis

# Chạy backend locally với IDE
# Sử dụng SPRING_PROFILES_ACTIVE=local
```

### Hot reload (dev mode)

Để có hot reload khi phát triển, bạn nên:
1. Chạy MySQL/Redis trong Docker
2. Chạy backend locally với IDE để có hot reload

---

## 📝 Ghi chú

- Volume `mysql_data` lưu trữ dữ liệu MySQL persistent
- Volume `redis_data` lưu trữ cache Redis
- Network `foodorder-network` cho phép các containers giao tiếp nội bộ

