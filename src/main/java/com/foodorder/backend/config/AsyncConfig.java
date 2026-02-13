package com.foodorder.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration để enable Spring Async
 * Cần thiết cho các @Async methods hoạt động (VD: gửi email thông báo contact, cập nhật totalLikes, totalShares)
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Cấu hình Thread Pool cho xử lý bất đồng bộ
     * - corePoolSize: Số thread tối thiểu
     * - maxPoolSize: Số thread tối đa
     * - queueCapacity: Số task chờ trong hàng đợi
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("AsyncThread-");
        executor.initialize();
        return executor;
    }
}

