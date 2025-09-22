package com.foodorder.backend.chatbot.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho response từ chatbot
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDTO {

    private String sessionId;

    private String message; // Tin nhắn phản hồi từ bot

    private String messageType; // "text", "suggestion", "product_recommendation"

    private LocalDateTime timestamp;

    private Integer responseTime; // Thời gian phản hồi (ms)

    private List<String> suggestions; // Gợi ý câu hỏi tiếp theo

    private List<QuickActionDTO> quickActions; // Các hành động nhanh

    private RecommendationDataDTO recommendationData; // Dữ liệu gợi ý sản phẩm

    private Boolean isFromKnowledgeBase; // Có phải từ knowledge base không

    private Double confidenceScore; // Điểm tin cậy của câu trả lời (0-1)

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuickActionDTO {
        private String label;
        private String action; // "view_menu", "place_order", "track_order", "contact_support"
        private String url;
        private Object data;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationDataDTO {
        private List<ProductRecommendationDTO> foods;
        private String reason; // Lý do gợi ý

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ProductRecommendationDTO {
            private Long id;
            private String name;
            private String description;
            private Double price;
            private String imageUrl;
            private String category;
            private Double rating;
        }
    }
}
