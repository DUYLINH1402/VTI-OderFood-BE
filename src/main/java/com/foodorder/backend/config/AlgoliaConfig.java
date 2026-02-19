package com.foodorder.backend.config;

import com.algolia.api.SearchClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình Algolia Search Client
 *
 * Algolia là dịch vụ tìm kiếm full-text search với hiệu suất cao
 * Các thông số cấu hình được lấy từ biến môi trường:
 * - ALGOLIA_APPLICATION_ID: Application ID từ Algolia dashboard
 * - ALGOLIA_API_KEY: Admin API Key có quyền write (không dùng Search-Only API Key)
 * - ALGOLIA_INDEX_NAME: Tên index để lưu trữ dữ liệu món ăn
 */
@Configuration
@Slf4j
public class AlgoliaConfig {

    @Value("${algolia.application-id}")
    private String applicationId;

    @Value("${algolia.api-key}")
    private String apiKey;

    /**
     * Tạo SearchClient Bean để inject vào các Service
     * SearchClient là client chính để tương tác với Algolia API
     */
    @Bean
    public SearchClient searchClient() {
        log.info("Khởi tạo Algolia SearchClient với Application ID: {}", applicationId);
        return new SearchClient(applicationId, apiKey);
    }
}

