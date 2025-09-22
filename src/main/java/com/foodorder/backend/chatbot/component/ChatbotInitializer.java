package com.foodorder.backend.chatbot.component;

import com.foodorder.backend.chatbot.service.KnowledgeBaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Component khởi tạo dữ liệu cho hệ thống Chatbot khi ứng dụng start
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(100) // Chạy sau khi các component khác đã khởi tạo xong
public class ChatbotInitializer implements ApplicationRunner {

    private final KnowledgeBaseService knowledgeBaseService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            // Khởi tạo dữ liệu mẫu cho knowledge base
            knowledgeBaseService.initializeSampleData();
        } catch (Exception e) {
            log.error("Lỗi khi khởi tạo dữ liệu Chatbot: {}", e.getMessage());
            // Không throw exception để không làm crash ứng dụng
        }
    }
}
