package com.foodorder.backend.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Arrays;
import java.util.Objects;

/**
 * Cấu hình Cache cho ứng dụng
 * Sử dụng in-memory cache với thời gian hết hạn 5 phút
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String ADMIN_USERS_CACHE = "adminUsers";
    public static final String ADMIN_USER_DETAILS_CACHE = "adminUserDetails";
    public static final String ADMIN_EMPLOYEES_CACHE = "adminEmployees";
    public static final String ORDER_STATISTICS_CACHE = "orderStatistics";
    public static final String ADMIN_ORDERS_CACHE = "adminOrders";
    public static final String DASHBOARD_STATISTICS_CACHE = "dashboardStatistics";
    public static final String DASHBOARD_REVENUE_CACHE = "dashboardRevenue";
    public static final String DASHBOARD_ACTIVITIES_CACHE = "dashboardActivities";
    public static final String ADMIN_FOODS_CACHE = "adminFoods";
    public static final String ADMIN_FOOD_DETAILS_CACHE = "adminFoodDetails";
    // Advanced Statistics Caches
    public static final String TOP_SELLING_FOODS_CACHE = "topSellingFoods";
    public static final String ADVANCED_STATISTICS_CACHE = "advancedStatistics";
    public static final String REVENUE_BY_CATEGORY_CACHE = "revenueByCategory";
    public static final String FOOD_PERFORMANCE_CACHE = "foodPerformance";
    // Promotion Statistics Caches
    public static final String COUPON_STATISTICS_CACHE = "couponStatisticsCache";
    public static final String POINTS_STATISTICS_CACHE = "pointsStatisticsCache";

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
                new ConcurrentMapCache(ADMIN_USERS_CACHE),
                new ConcurrentMapCache(ADMIN_USER_DETAILS_CACHE),
                new ConcurrentMapCache(ADMIN_EMPLOYEES_CACHE),
                new ConcurrentMapCache(ORDER_STATISTICS_CACHE),
                new ConcurrentMapCache(ADMIN_ORDERS_CACHE),
                new ConcurrentMapCache(DASHBOARD_STATISTICS_CACHE),
                new ConcurrentMapCache(DASHBOARD_REVENUE_CACHE),
                new ConcurrentMapCache(DASHBOARD_ACTIVITIES_CACHE),
                new ConcurrentMapCache(ADMIN_FOODS_CACHE),
                new ConcurrentMapCache(ADMIN_FOOD_DETAILS_CACHE),
                // Advanced Statistics Caches
                new ConcurrentMapCache(TOP_SELLING_FOODS_CACHE),
                new ConcurrentMapCache(ADVANCED_STATISTICS_CACHE),
                new ConcurrentMapCache(REVENUE_BY_CATEGORY_CACHE),
                new ConcurrentMapCache(FOOD_PERFORMANCE_CACHE),
                // Promotion Statistics Caches
                new ConcurrentMapCache(COUPON_STATISTICS_CACHE),
                new ConcurrentMapCache(POINTS_STATISTICS_CACHE)
        ));
        return cacheManager;
    }

    /**
     * Tự động xóa cache mỗi 5 phút
     */
    @Scheduled(fixedRate = 300000) // 5 phút = 300,000 ms
    public void evictAllCaches() {
        CacheManager cacheManager = cacheManager();
        cacheManager.getCacheNames()
                .forEach(cacheName -> Objects.requireNonNull(cacheManager.getCache(cacheName)).clear());
    }
}
