<div align="center">

# Food Order Backend

### Online Food Ordering System - Backend API

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen?style=for-the-badge&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk)](https://openjdk.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-7-red?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)

<p align="center">
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square" alt="License">
  <img src="https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square" alt="PRs Welcome">
</p>

---

**Backend RESTful API** for online food ordering system with full features including authentication, order management, payment integration and more.

[Features](#1-features) •
[Technologies](#2-technologies) •
[Installation](#4-installation) •
[API Docs](#5-api-documentation) •
[Architecture](#6-system-architecture)

</div>

---

## 1. Features

<table>
<tr>
<td>

### Authentication & Authorization
- JWT Authentication
- OAuth2 (Google, Facebook)
- Email verification
- Password reset
- Role-based access (User/Staff/Admin)

</td>
<td>

### Food Management
- CRUD operations with images
- Category classification
- Search with Algolia
- Favorites & reviews

</td>
</tr>
<tr>
<td>

### Cart & Orders
- Real-time cart management
- Create & track orders
- Order history
- Coupon discounts

</td>
<td>

### Payment
- ZaloPay integration
- Cash on Delivery (COD)
- Callback & webhook handling
- Transaction tracking

</td>
</tr>
<tr>
<td>

### Interaction
- AI Chatbot (OpenAI)
- Real-time chat (WebSocket)
- Comments & Reviews
- Like & Share

</td>
<td>

### Analytics & Admin
- Statistics dashboard
- User management
- Reward points system
- Email notifications

</td>
</tr>
</table>

---

## 2. Technologies

<div align="center">

| Category | Technologies |
|----------|-------------|
| **Framework** | ![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat-square&logo=spring-boot&logoColor=white) ![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=flat-square&logo=spring-security&logoColor=white) |
| **Database** | ![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white) ![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white) |
| **Authentication** | ![JWT](https://img.shields.io/badge/JWT-000000?style=flat-square&logo=json-web-tokens&logoColor=white) ![OAuth2](https://img.shields.io/badge/OAuth2-4285F4?style=flat-square&logo=google&logoColor=white) |
| **Cloud Services** | ![AWS S3](https://img.shields.io/badge/AWS_S3-569A31?style=flat-square&logo=amazon-s3&logoColor=white) ![Algolia](https://img.shields.io/badge/Algolia-003DFF?style=flat-square&logo=algolia&logoColor=white) |
| **DevOps** | ![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat-square&logo=docker&logoColor=white) ![Maven](https://img.shields.io/badge/Maven-C71A36?style=flat-square&logo=apache-maven&logoColor=white) |
| **Communication** | ![WebSocket](https://img.shields.io/badge/WebSocket-010101?style=flat-square&logo=socket.io&logoColor=white) ![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=flat-square&logo=thymeleaf&logoColor=white) |

</div>

---

## 📁 3. Project Structure

```
src/main/java/com/foodorder/backend/
├── 📂 auth/           # Authentication & Authorization
├── 📂 blog/           # Blog management
├── 📂 cart/           # Shopping cart
├── 📂 category/       # Food categories
├── 📂 chat/           # Real-time chat
├── 📂 chatbot/        # AI Chatbot integration
├── 📂 comment/        # Comments system
├── 📂 config/         # Application configurations
├── 📂 contact/        # Contact form
├── 📂 coupons/        # Coupon management
├── 📂 dashboard/      # Admin dashboard
├── 📂 exception/      # Global exception handling
├── 📂 favorite/       # Favorites management
├── 📂 feedbacks/      # User feedbacks
├── 📂 food/           # Food management
├── 📂 like/           # Like system
├── 📂 notifications/  # Notification service
├── 📂 order/          # Order management
├── 📂 payments/       # Payment integration
├── 📂 points/         # Reward points
├── 📂 search/         # Search functionality
├── 📂 security/       # Security configurations
├── 📂 service/        # Common services
├── 📂 share/          # Share functionality
├── 📂 user/           # User management
├── 📂 util/           # Utility classes
├── 📂 validation/     # Custom validators
├── 📂 websocket/      # WebSocket configuration
└── 📂 zone/           # Delivery zones
```

---

## 4. Installation

### System Requirements

- **Java** 17+
- **Maven** 3.8+
- **Docker** & **Docker Compose** (recommended)
- **MySQL** 8.0
- **Redis** 7.0

### Installation with Docker (Recommended)

1. **Clone repository**
   ```bash
   git clone https://github.com/DUYLINH1402/OrderFood-BE-Java.git
   cd food-order-backend
   ```

2. **Create `.env` file**
   ```bash
   cp .env.example .env
   ```

3. **Configure environment variables** in `.env` file:
   ```env
   # Database
   MYSQL_USERNAME=your_username
   MYSQL_PASSWORD=your_password
   
   # JWT
   JWT_SECRET=your_jwt_secret_key
   JWT_EXPIRATION=86400000
   
   # AWS S3
   AWS_ACCESS_KEY_ID=your_access_key
   AWS_SECRET_ACCESS_KEY=your_secret_key
   AWS_REGION=ap-southeast-1
   
   # Email (Brevo)
   BREVO_API_KEY=your_brevo_api_key
   BREVO_SENDER_EMAIL=noreply@yourstore.com
   
   # OAuth2
   GOOGLE_CLIENT_ID=your_google_client_id
   GOOGLE_CLIENT_SECRET=your_google_client_secret
   FACEBOOK_APP_ID=your_facebook_app_id
   FACEBOOK_APP_SECRET=your_facebook_app_secret
   
   # ZaloPay
   ZALOPAY_APP_ID=your_app_id
   ZALOPAY_KEY1=your_key1
   ZALOPAY_KEY2=your_key2
   
   # OpenAI
   OPENAI_API_KEY=your_openai_api_key
   
   # Algolia
   ALGOLIA_APPLICATION_ID=your_app_id
   ALGOLIA_API_KEY=your_api_key
   ```

4. **Start with Docker Compose**
   ```bash
   docker-compose up -d
   ```

5. **Verify the application**
   ```
   http://localhost:8080/actuator/health
   ```

### Manual Installation (Development)

1. **Clone and configure**
   ```bash
   git clone https://github.com/DUYLINH1402/OrderFood-BE-Java.git
   cd food-order-backend
   cp .env.example .env
   # Edit .env with your configuration
   ```

2. **Install MySQL & Redis**
   ```bash
   # Use docker compose for database
   docker compose -f docker-compose.yml up -d
   ```

3. **Build and run**
   ```bash
   ./mvnw clean install
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
   ```

---

## 5. API Documentation

After starting, access Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

### Main Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/auth/register` | Register account |
| `POST` | `/api/auth/login` | Login |
| `GET` | `/api/foods` | Get food list |
| `GET` | `/api/foods/{id}` | Food details |
| `POST` | `/api/cart` | Add to cart |
| `POST` | `/api/orders` | Create order |
| `GET` | `/api/orders/{id}` | Order details |


---

## 6. System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        CLIENT                               │
│                  (Web App / Mobile App)                     │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                    API GATEWAY                              │
│                  (Spring Security)                          │
│          JWT Authentication | OAuth2 | CORS                 │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                   CONTROLLER LAYER                          │
│         REST Controllers | WebSocket Handlers               │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                    SERVICE LAYER                            │
│              Business Logic | Validation                    │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                  REPOSITORY LAYER                           │
│                   Spring Data JPA                           │
└──────────────┬──────────────────────────┬───────────────────┘
               │                          │
               ▼                          ▼
┌──────────────────────┐    ┌─────────────────────────────────┐
│      MySQL 8.0       │    │         Redis Cache             │
│  (Primary Database)  │    │  (Session & Response Caching)   │
└──────────────────────┘    └─────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────────┐
│                  EXTERNAL SERVICES                          │
├─────────────┬──────────────┬─────────────┬──────────────────┤
│   AWS S3    │   ZaloPay    │   Algolia   │    OpenAI        │
│  (Storage)  │  (Payment)   │  (Search)   │   (Chatbot)      │
└─────────────┴──────────────┴─────────────┴──────────────────┘
```

---

## 7. Security

- **JWT Token** with refresh token rotation
- **OAuth2** integration (Google, Facebook)
- **Password encoding** with BCrypt
- **Role-based Access Control** (RBAC)
- **Rate limiting** & request validation
- **CORS** configuration
- **SQL Injection** protection with JPA
- **XSS** protection

---

## 8. Caching Strategy

The project uses **Redis** for caching with different TTLs:

| Cache Type | TTL | Description |
|------------|-----|-------------|
| `COMMENTS_CACHE` | 3 min | Comments |
| `FOODS_CACHE` | 5 min | Food list |
| `FOOD_DETAIL_CACHE` | 5 min | Food details |
| `DASHBOARD_CACHE` | 15 min | Dashboard statistics |
| `CATEGORIES_CACHE` | 30 min | Categories |



---

## 9. Testing

```bash
# Run unit tests
./mvnw test

# Run integration tests
./mvnw verify

# Test coverage report
./mvnw jacoco:report
```

---

## 10. Environment Variables

| Variable | Description | Required |
|----------|-------------|----------|
| `MYSQL_USERNAME` | MySQL username | Yes |
| `MYSQL_PASSWORD` | MySQL password | Yes |
| `JWT_SECRET` | JWT signing key | Yes |
| `JWT_EXPIRATION` | JWT token expiration (ms) | No |
| `AWS_ACCESS_KEY_ID` | AWS access key | Yes |
| `AWS_SECRET_ACCESS_KEY` | AWS secret key | Yes |
| `BREVO_API_KEY` | Brevo email API key | Yes |
| `GOOGLE_CLIENT_ID` | Google OAuth client ID | No |
| `FACEBOOK_APP_ID` | Facebook OAuth app ID | No |
| `ZALOPAY_APP_ID` | ZaloPay application ID | No |
| `OPENAI_API_KEY` | OpenAI API key | No |
| `ALGOLIA_APPLICATION_ID` | Algolia app ID | No |

---

## 11. Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 12. Contact

<div align="center">

[![Email](https://img.shields.io/badge/Email-D14836?style=for-the-badge&logo=gmail&logoColor=white)](mailto:duylinh63b5@gmail.com)
[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/DUYLINH1402)

</div>

---

<div align="center">

### Star this repo if you find it helpful!

Made with love by [DuyLinh](https://github.com/DUYLINH1402)

</div>
