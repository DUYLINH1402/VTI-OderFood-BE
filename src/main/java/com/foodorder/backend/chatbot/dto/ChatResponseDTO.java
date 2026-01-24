package com.foodorder.backend.chatbot.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Response chứa phản hồi từ chatbot AI")
public class ChatResponseDTO {

    @Schema(description = "Session ID của cuộc hội thoại", example = "session_abc123")
    private String sessionId;

    @Schema(description = "Tin nhắn phản hồi từ bot", example = "Chào bạn! Tôi có thể giúp gì cho bạn?")
    private String message;

    @Schema(description = "Loại tin nhắn", example = "text", allowableValues = {"text", "suggestion", "product_recommendation"})
    private String messageType;

    @Schema(description = "Thời gian phản hồi", example = "2025-01-20T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "Thời gian xử lý phản hồi (ms)", example = "150")
    private Integer responseTime;

    @Schema(description = "Danh sách gợi ý câu hỏi tiếp theo")
    private List<String> suggestions;

    @Schema(description = "Danh sách các hành động nhanh")
    private List<QuickActionDTO> quickActions;

    @Schema(description = "Dữ liệu gợi ý sản phẩm")
    private RecommendationDataDTO recommendationData;

    @Schema(description = "Có phải từ knowledge base không", example = "true")
    private Boolean isFromKnowledgeBase;

    @Schema(description = "Điểm tin cậy của câu trả lời (0-1)", example = "0.95")
    private Double confidenceScore;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Hành động nhanh cho người dùng")
    public static class QuickActionDTO {
        @Schema(description = "Nhãn hiển thị", example = "Xem thực đơn")
        private String label;

        @Schema(description = "Loại hành động", example = "view_menu", allowableValues = {"view_menu", "place_order", "track_order", "contact_support"})
        private String action;

        @Schema(description = "URL liên kết (nếu có)", example = "/menu")
        private String url;

        @Schema(description = "Dữ liệu bổ sung cho hành động")
        private Object data;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Dữ liệu gợi ý sản phẩm")
    public static class RecommendationDataDTO {
        @Schema(description = "Danh sách món ăn được gợi ý")
        private List<ProductRecommendationDTO> foods;

        @Schema(description = "Lý do gợi ý", example = "Dựa trên sở thích của bạn")
        private String reason;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        @Schema(description = "Thông tin món ăn được gợi ý")
        public static class ProductRecommendationDTO {
            @Schema(description = "ID món ăn", example = "1")
            private Long id;

            @Schema(description = "Tên món ăn", example = "Phở bò tái")
            private String name;

            @Schema(description = "Mô tả món ăn", example = "Phở bò tái chín với nước dùng đậm đà")
            private String description;

            @Schema(description = "Giá món ăn", example = "55000")
            private Double price;

            @Schema(description = "URL hình ảnh", example = "https://example.com/pho.jpg")
            private String imageUrl;

            @Schema(description = "Danh mục món ăn", example = "Món chính")
            private String category;

            @Schema(description = "Đánh giá trung bình", example = "4.5")
            private Double rating;
        }
    }
}
