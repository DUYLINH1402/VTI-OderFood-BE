# ===========================================
# Dockerfile cho Food Order Backend (Java 17)
# ===========================================

# Stage 1: Build ứng dụng với Maven
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Copy file pom.xml và tải dependencies trước (cache layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code và build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Chạy ứng dụng
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Thêm user không phải root để chạy ứng dụng (bảo mật)
RUN addgroup -S spring && adduser -S spring -G spring

# Copy file JAR từ stage build
COPY --from=builder /app/target/*.jar app.jar

# Đổi quyền sở hữu cho user spring
RUN chown -R spring:spring /app

USER spring

# Expose port
EXPOSE 8081

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
