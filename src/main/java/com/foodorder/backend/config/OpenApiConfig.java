
package com.foodorder.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Cấu hình OpenAPI (Swagger) cho API documentation
 * OpenAPI JSON: /v3/api-docs
 *
 * API được chia thành các nhóm theo Role:
 * - Public: Các API công khai không cần xác thực
 * - User: Các API dành cho người dùng đã đăng nhập
 * - Staff: Các API dành cho nhân viên
 * - Admin: Các API dành cho quản trị viên
 */
@Configuration
public class OpenApiConfig {

    @Value("${spring.application.name:Food Order Backend}")
    private String applicationName;

    /**
     * Cấu hình OpenAPI với thông tin API và JWT Security
     */
    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                // Thông tin API
                .info(new Info()
                        .title("Food Order API")
                        .description("RESTful API cho hệ thống đặt món ăn trực tuyến. " +
                                "Hỗ trợ các chức năng: xác thực người dùng, quản lý thực đơn, " +
                                "giỏ hàng, đơn hàng, thanh toán, điểm thưởng và nhiều hơn nữa.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Food Order Team")
                                .email("support@foodorder.com")
                                .url("https://dongxanhfood.shop"))
                        .license(new License()
                                .name("Private License")
                                .url("https://dongxanhfood.shop")))

                // Server endpoints
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://dongxanhfoodorder.shop")
                                .description("Production Server")))

                // Cấu hình JWT Bearer Authentication
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Nhập JWT token để xác thực. " +
                                                "Token được lấy từ API /api/auth/login")));
    }

    /**
     * Nhóm API Public - Các API công khai không cần xác thực
     * Bao gồm: Auth (đăng nhập, đăng ký), Foods (xem danh sách), Categories, Districts, Wards
     */
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("1. Public APIs")
                .displayName("🌐 Public - Công khai")
                .pathsToMatch(
                        "/api/auth/**",
                        "/api/foods/**",
                        "/api/categories/**",
                        "/api/districts/**",
                        "/api/wards/**",
                        "/api/chatbot/**"
                )
                .pathsToExclude(
                        "/api/admin/**",
                        "/api/staff/**"
                )
                .build();
    }

    /**
     * Nhóm API User - Các API dành cho người dùng đã đăng nhập
     * Bao gồm: Cart, Orders, Points, Coupons, Favorites, Notifications, Payments, Chat, Feedback
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("2. User APIs")
                .displayName("👤 User - Người dùng")
                .pathsToMatch(
                        "/api/cart/**",
                        "/api/orders/**",
                        "/api/v1/orders/**",
                        "/api/points/**",
                        "/api/coupons/**",
                        "/api/favorites/**",
                        "/api/notifications/**",
                        "/api/notifications/user/**",
                        "/api/payments/**",
                        "/api/chat/**",
                        "/api/feedback-media/**",
                        "/api/users/**"
                )
                .pathsToExclude(
                        "/api/admin/**",
                        "/api/staff/**",
                        "/api/v1/admin/**",
                        "/api/notifications/staff/**"
                )
                .build();
    }

    /**
     * Nhóm API Staff - Các API dành cho nhân viên
     * Bao gồm: Quản lý đơn hàng của nhân viên, Notifications cho staff, Chat
     */
    @Bean
    public GroupedOpenApi staffApi() {
        return GroupedOpenApi.builder()
                .group("3. Staff APIs")
                .displayName("👷 Staff - Nhân viên")
                .pathsToMatch(
                        "/api/staff/**",
                        "/api/notifications/staff/**",
                        "/api/chat/**"
                )
                .build();
    }

    /**
     * Nhóm API Admin - Các API dành cho quản trị viên
     * Bao gồm: Quản lý Foods, Orders, Users, Employees, Dashboard, Coupons, Points Statistics
     */
    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("4. Admin APIs")
                .displayName("🔑 Admin - Quản trị")
                .pathsToMatch(
                        "/api/admin/**",
                        "/api/v1/admin/**"
                )
                .build();
    }

    /**
     * Nhóm tất cả API - Hiển thị toàn bộ endpoints
     */
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("5. All APIs")
                .displayName("📋 Tất cả APIs")
                .pathsToMatch("/api/**")
                .build();
    }
}
