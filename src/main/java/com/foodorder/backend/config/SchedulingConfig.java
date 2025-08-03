package com.foodorder.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Configuration để enable Spring Scheduling
 * Cần thiết cho CouponScheduler hoạt động
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
    // Spring sẽ tự động detect và chạy các @Scheduled methods
}
