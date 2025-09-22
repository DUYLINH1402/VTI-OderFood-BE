package com.foodorder.backend.chatbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Cấu hình WebClient cho các dịch vụ external API
 */
@Configuration
public class ChatbotConfig {

    /**
     * WebClient cho OpenAI API
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
