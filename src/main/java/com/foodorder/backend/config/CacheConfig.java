package com.foodorder.backend.config;

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

    /**
     * Cấu hình RedisCacheManager với TTL riêng cho từng loại cache
     *
     * @param connectionFactory Redis connection factory
     * @return CacheManager đã được cấu hình
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Cấu hình mặc định cho cache
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(TTL_DEFAULT)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
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

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .transactionAware()
                .build();
    }
}
