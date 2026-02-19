package com.foodorder.backend.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Cấu hình Redis Cache cho ứng dụng
 *
 * TTL được thiết kế dựa trên tính chất của từng loại dữ liệu:
 * - Dữ liệu ít thay đổi (danh mục, cấu hình): 30 phút - 1 giờ
 * - Dữ liệu thống kê (dashboard, reports): 10-15 phút
 * - Dữ liệu danh sách admin (users, orders, foods): 5-10 phút
 * - Dữ liệu chi tiết (food details, user details): 5 phút
 * - Dữ liệu user-facing (comments, blogs): 3-5 phút
 *
 * Khi có thao tác CUD (Create/Update/Delete), cache sẽ tự động evict
 * thông qua annotation @CacheEvict đã được đặt trong các Service
 */
@Configuration
@EnableCaching
public class CacheConfig {

    // =============================================
    // ADMIN USER CACHES
    // =============================================
    /** Cache danh sách users trong admin - TTL: 10 phút */
    public static final String ADMIN_USERS_CACHE = "adminUsers";
    /** Cache chi tiết user trong admin - TTL: 5 phút */
    public static final String ADMIN_USER_DETAILS_CACHE = "adminUserDetails";
    /** Cache danh sách nhân viên - TTL: 10 phút */
    public static final String ADMIN_EMPLOYEES_CACHE = "adminEmployees";

    // =============================================
    // ORDER CACHES
    // =============================================
    /** Cache thống kê đơn hàng - TTL: 10 phút */
    public static final String ORDER_STATISTICS_CACHE = "orderStatistics";
    /** Cache danh sách đơn hàng admin - TTL: 5 phút (thường xuyên thay đổi) */
    public static final String ADMIN_ORDERS_CACHE = "adminOrders";

    // =============================================
    // DASHBOARD CACHES
    // =============================================
    /** Cache thống kê tổng quan dashboard - TTL: 15 phút */
    public static final String DASHBOARD_STATISTICS_CACHE = "dashboardStatistics";
    /** Cache doanh thu dashboard - TTL: 15 phút */
    public static final String DASHBOARD_REVENUE_CACHE = "dashboardRevenue";
    /** Cache hoạt động gần đây - TTL: 5 phút */
    public static final String DASHBOARD_ACTIVITIES_CACHE = "dashboardActivities";

    // =============================================
    // FOOD CACHES
    // =============================================
    /** Cache danh sách món ăn admin - TTL: 10 phút */
    public static final String ADMIN_FOODS_CACHE = "adminFoods";
    /** Cache chi tiết món ăn - TTL: 5 phút */
    public static final String ADMIN_FOOD_DETAILS_CACHE = "adminFoodDetails";

    // =============================================
    // FOOD PUBLIC CACHES
    // =============================================
    /** Cache danh sách tất cả món ăn - TTL: 5 phút */
    public static final String FOODS_ALL_CACHE = "foodsAll";
    /** Cache danh sách món ăn mới - TTL: 5 phút */
    public static final String FOODS_NEW_CACHE = "foodsNew";
    /** Cache danh sách món ăn nổi bật - TTL: 5 phút */
    public static final String FOODS_FEATURED_CACHE = "foodsFeatured";
    /** Cache danh sách món ăn bán chạy - TTL: 5 phút */
    public static final String FOODS_BESTSELLER_CACHE = "foodsBestseller";
    /** Cache món ăn theo danh mục - TTL: 5 phút */
    public static final String FOODS_BY_CATEGORY_CACHE = "foodsByCategory";
    /** Cache chi tiết món ăn theo ID - TTL: 5 phút */
    public static final String FOOD_DETAIL_CACHE = "foodDetail";
    /** Cache chi tiết món ăn theo Slug - TTL: 5 phút */
    public static final String FOOD_DETAIL_SLUG_CACHE = "foodDetailSlug";
    /** Cache danh sách món ăn quản lý (Staff) - TTL: 5 phút */
    public static final String FOODS_MANAGEMENT_CACHE = "foodsManagement";

    // =============================================
    // ADVANCED STATISTICS CACHES
    // =============================================
    /** Cache top món bán chạy - TTL: 15 phút */
    public static final String TOP_SELLING_FOODS_CACHE = "topSellingFoods";
    /** Cache thống kê nâng cao - TTL: 15 phút */
    public static final String ADVANCED_STATISTICS_CACHE = "advancedStatistics";
    /** Cache doanh thu theo danh mục - TTL: 15 phút */
    public static final String REVENUE_BY_CATEGORY_CACHE = "revenueByCategory";
    /** Cache hiệu suất món ăn - TTL: 15 phút */
    public static final String FOOD_PERFORMANCE_CACHE = "foodPerformance";

    // =============================================
    // PROMOTION STATISTICS CACHES
    // =============================================
    /** Cache thống kê coupon - TTL: 10 phút */
    public static final String COUPON_STATISTICS_CACHE = "couponStatisticsCache";
    /** Cache thống kê điểm thưởng - TTL: 10 phút */
    public static final String POINTS_STATISTICS_CACHE = "pointsStatisticsCache";

    // =============================================
    // COMMENT CACHES
    // =============================================
    /** Cache comments theo target (food, blog) - TTL: 3 phút (tương tác nhiều) */
    public static final String COMMENTS_BY_TARGET_CACHE = "commentsByTarget";
    /** Cache số lượng comment - TTL: 3 phút */
    public static final String COMMENT_COUNT_CACHE = "commentCount";
    /** Cache comments cho admin - TTL: 5 phút */
    public static final String ADMIN_COMMENTS_CACHE = "adminComments";

    // =============================================
    // BLOG CACHES
    // =============================================
    /** Cache danh sách bài viết - TTL: 10 phút */
    public static final String BLOGS_CACHE = "blogs";
    /** Cache bài viết nổi bật - TTL: 30 phút (ít thay đổi) */
    public static final String FEATURED_BLOGS_CACHE = "featuredBlogs";
    /** Cache danh mục blog - TTL: 30 phút (ít thay đổi) */
    public static final String BLOG_CATEGORIES_CACHE = "blogCategories";
    /** Cache blogs theo loại - TTL: 10 phút */
    public static final String BLOGS_BY_TYPE_CACHE = "blogsByType";
    /** Cache blogs theo danh mục - TTL: 10 phút */
    public static final String BLOGS_BY_CATEGORY_CACHE = "blogsByCategory";
    /** Cache bài viết liên quan - TTL: 10 phút */
    public static final String RELATED_BLOGS_CACHE = "relatedBlogs";

    // =============================================
    // CATEGORY CACHES
    // =============================================
    /** Cache danh sách danh mục - TTL: 30 phút (ít thay đổi) */
    public static final String CATEGORIES_CACHE = "categories";
    /** Cache chi tiết danh mục - TTL: 30 phút */
    public static final String CATEGORY_DETAIL_CACHE = "categoryDetail";
    /** Cache danh mục gốc - TTL: 30 phút */
    public static final String ROOT_CATEGORIES_CACHE = "rootCategories";
    /** Cache danh mục con - TTL: 30 phút */
    public static final String CHILD_CATEGORIES_CACHE = "childCategories";

    // =============================================
    // ZONE CACHES (Địa chỉ)
    // =============================================
    /** Cache danh sách quận/huyện - TTL: 1 giờ (rất ít thay đổi) */
    public static final String DISTRICTS_CACHE = "districts";
    /** Cache danh sách phường/xã theo quận - TTL: 1 giờ */
    public static final String WARDS_BY_DISTRICT_CACHE = "wardsByDistrict";

    // =============================================
    // COUPON PUBLIC CACHES
    // =============================================
    /** Cache danh sách coupon công khai - TTL: 5 phút */
    public static final String ACTIVE_COUPONS_CACHE = "activeCoupons";

    // =============================================
    // TTL DURATIONS (Thời gian hết hạn cache)
    // =============================================
    /** TTL ngắn: 3 phút - Dữ liệu thay đổi thường xuyên (comments) */
    private static final Duration TTL_SHORT = Duration.ofMinutes(3);
    /** TTL mặc định: 5 phút - Dữ liệu chi tiết, danh sách nhỏ */
    private static final Duration TTL_DEFAULT = Duration.ofMinutes(5);
    /** TTL trung bình: 10 phút - Danh sách admin, thống kê thường */
    private static final Duration TTL_MEDIUM = Duration.ofMinutes(10);
    /** TTL dài: 15 phút - Thống kê dashboard, reports */
    private static final Duration TTL_LONG = Duration.ofMinutes(15);
    /** TTL rất dài: 30 phút - Dữ liệu ít thay đổi (danh mục, featured) */
    private static final Duration TTL_VERY_LONG = Duration.ofMinutes(30);
    /** TTL cực dài: 1 giờ - Dữ liệu hầu như không đổi (địa chỉ, config) */
    private static final Duration TTL_EXTRA_LONG = Duration.ofHours(1);

    /**
     * Tạo ObjectMapper hỗ trợ Java 8 date/time types (LocalDateTime, LocalDate, etc.)
     * Cần thiết để Redis có thể serialize/deserialize các object chứa LocalDateTime
     *
     * @return ObjectMapper đã được cấu hình
     */
    private ObjectMapper createRedisObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Đăng ký JavaTimeModule để hỗ trợ LocalDateTime, LocalDate, etc.
        objectMapper.registerModule(new JavaTimeModule());

        // Tắt việc serialize dates dạng timestamps, serialize thành ISO-8601 string
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Kích hoạt default typing để lưu thông tin class khi serialize
        // Điều này cần thiết để deserialize đúng object type từ Redis
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        return objectMapper;
    }

    /**
     * Cấu hình RedisCacheManager với TTL riêng cho từng loại cache
     *
     * @param connectionFactory Redis connection factory
     * @return CacheManager đã được cấu hình
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Tạo ObjectMapper hỗ trợ Java 8 date/time
        ObjectMapper objectMapper = createRedisObjectMapper();

        // Tạo serializer với ObjectMapper đã cấu hình
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // Cấu hình mặc định cho cache
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(TTL_DEFAULT)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))
                .disableCachingNullValues();

        // Cấu hình TTL riêng cho từng cache
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // ADMIN USER CACHES
        cacheConfigurations.put(ADMIN_USERS_CACHE, defaultConfig.entryTtl(TTL_MEDIUM));
        cacheConfigurations.put(ADMIN_USER_DETAILS_CACHE, defaultConfig.entryTtl(TTL_DEFAULT));
        cacheConfigurations.put(ADMIN_EMPLOYEES_CACHE, defaultConfig.entryTtl(TTL_MEDIUM));

        // ORDER CACHES
        cacheConfigurations.put(ORDER_STATISTICS_CACHE, defaultConfig.entryTtl(TTL_MEDIUM));
        cacheConfigurations.put(ADMIN_ORDERS_CACHE, defaultConfig.entryTtl(TTL_DEFAULT));

        // DASHBOARD CACHES
        cacheConfigurations.put(DASHBOARD_STATISTICS_CACHE, defaultConfig.entryTtl(TTL_LONG));
        cacheConfigurations.put(DASHBOARD_REVENUE_CACHE, defaultConfig.entryTtl(TTL_LONG));
        cacheConfigurations.put(DASHBOARD_ACTIVITIES_CACHE, defaultConfig.entryTtl(TTL_DEFAULT));

        // FOOD CACHES
        cacheConfigurations.put(ADMIN_FOODS_CACHE, defaultConfig.entryTtl(TTL_MEDIUM));
        cacheConfigurations.put(ADMIN_FOOD_DETAILS_CACHE, defaultConfig.entryTtl(TTL_DEFAULT));

        // FOOD PUBLIC CACHES
        cacheConfigurations.put(FOODS_ALL_CACHE, defaultConfig.entryTtl(TTL_DEFAULT));
        cacheConfigurations.put(FOODS_NEW_CACHE, defaultConfig.entryTtl(TTL_DEFAULT));
        cacheConfigurations.put(FOODS_FEATURED_CACHE, defaultConfig.entryTtl(TTL_DEFAULT));
        cacheConfigurations.put(FOODS_BESTSELLER_CACHE, defaultConfig.entryTtl(TTL_DEFAULT));
        cacheConfigurations.put(FOODS_BY_CATEGORY_CACHE, defaultConfig.entryTtl(TTL_DEFAULT));
        cacheConfigurations.put(FOOD_DETAIL_CACHE, defaultConfig.entryTtl(TTL_DEFAULT));
        cacheConfigurations.put(FOOD_DETAIL_SLUG_CACHE, defaultConfig.entryTtl(TTL_DEFAULT));
        cacheConfigurations.put(FOODS_MANAGEMENT_CACHE, defaultConfig.entryTtl(TTL_DEFAULT));

        // ADVANCED STATISTICS CACHES
        cacheConfigurations.put(TOP_SELLING_FOODS_CACHE, defaultConfig.entryTtl(TTL_LONG));
        cacheConfigurations.put(ADVANCED_STATISTICS_CACHE, defaultConfig.entryTtl(TTL_LONG));
        cacheConfigurations.put(REVENUE_BY_CATEGORY_CACHE, defaultConfig.entryTtl(TTL_LONG));
        cacheConfigurations.put(FOOD_PERFORMANCE_CACHE, defaultConfig.entryTtl(TTL_LONG));

        // PROMOTION STATISTICS CACHES
        cacheConfigurations.put(COUPON_STATISTICS_CACHE, defaultConfig.entryTtl(TTL_MEDIUM));
        cacheConfigurations.put(POINTS_STATISTICS_CACHE, defaultConfig.entryTtl(TTL_MEDIUM));

        // COMMENT CACHES
        cacheConfigurations.put(COMMENTS_BY_TARGET_CACHE, defaultConfig.entryTtl(TTL_SHORT));
        cacheConfigurations.put(COMMENT_COUNT_CACHE, defaultConfig.entryTtl(TTL_SHORT));
        cacheConfigurations.put(ADMIN_COMMENTS_CACHE, defaultConfig.entryTtl(TTL_DEFAULT));

        // BLOG CACHES
        cacheConfigurations.put(BLOGS_CACHE, defaultConfig.entryTtl(TTL_MEDIUM));
        cacheConfigurations.put(FEATURED_BLOGS_CACHE, defaultConfig.entryTtl(TTL_VERY_LONG));
        cacheConfigurations.put(BLOG_CATEGORIES_CACHE, defaultConfig.entryTtl(TTL_VERY_LONG));
        cacheConfigurations.put(BLOGS_BY_TYPE_CACHE, defaultConfig.entryTtl(TTL_MEDIUM));
        cacheConfigurations.put(BLOGS_BY_CATEGORY_CACHE, defaultConfig.entryTtl(TTL_MEDIUM));
        cacheConfigurations.put(RELATED_BLOGS_CACHE, defaultConfig.entryTtl(TTL_MEDIUM));

        // CATEGORY CACHES
        cacheConfigurations.put(CATEGORIES_CACHE, defaultConfig.entryTtl(TTL_VERY_LONG));
        cacheConfigurations.put(CATEGORY_DETAIL_CACHE, defaultConfig.entryTtl(TTL_VERY_LONG));
        cacheConfigurations.put(ROOT_CATEGORIES_CACHE, defaultConfig.entryTtl(TTL_VERY_LONG));
        cacheConfigurations.put(CHILD_CATEGORIES_CACHE, defaultConfig.entryTtl(TTL_VERY_LONG));

        // ZONE CACHES
        cacheConfigurations.put(DISTRICTS_CACHE, defaultConfig.entryTtl(TTL_EXTRA_LONG));
        cacheConfigurations.put(WARDS_BY_DISTRICT_CACHE, defaultConfig.entryTtl(TTL_EXTRA_LONG));

        // COUPON PUBLIC CACHES
        cacheConfigurations.put(ACTIVE_COUPONS_CACHE, defaultConfig.entryTtl(TTL_DEFAULT));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}
