package com.foodorder.backend.chatbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * Service tích hợp với OpenAI API để xử lý các yêu cầu chat
 */
@Service
@Slf4j
public class OpenAIService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model:gpt-4o}")
    private String model;

    @Value("${openai.max-tokens:1000}")
    private Integer maxTokens;

    @Value("${openai.temperature:0.7}")
    private Double temperature;

    public OpenAIService(WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        this.webClient = webClientBuilder
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.objectMapper = objectMapper;
    }

    /**
     * Gửi tin nhắn đến OpenAI và nhận phản hồi
     */
    public Mono<String> getChatCompletion(String userMessage, String systemPrompt, List<String> conversationHistory) {
        try {
            // Tạo messages array cho API
            List<Map<String, Object>> messages = new ArrayList<>();

            // Thêm system message
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                messages.add(Map.of(
                    "role", "system",
                    "content", systemPrompt
                ));
            }

            // Thêm lịch sử hội thoại (giới hạn để tránh vượt quá token limit)
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                int historyLimit = Math.min(conversationHistory.size(), 10);
                for (int i = conversationHistory.size() - historyLimit; i < conversationHistory.size(); i++) {
                    String msg = conversationHistory.get(i);
                    // Giả định format: "USER: ... " hoặc "BOT: ..."
                    if (msg.startsWith("USER: ")) {
                        messages.add(Map.of(
                            "role", "user",
                            "content", msg.substring(6)
                        ));
                    } else if (msg.startsWith("BOT: ")) {
                        messages.add(Map.of(
                            "role", "assistant",
                            "content", msg.substring(5)
                        ));
                    }
                }
            }

            // Thêm tin nhắn hiện tại của user
            messages.add(Map.of(
                "role", "user",
                "content", userMessage
            ));

            // Tạo request body
            Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", messages,
                "max_tokens", maxTokens,
                "temperature", temperature,
                "stream", false
            );

//            log.info("Gửi request đến OpenAI với {} messages", messages.size());

            return webClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(this::extractContentFromResponse)
                    .doOnError(error -> log.error("Lỗi khi gọi OpenAI API: {}", error.getMessage()))
                    .onErrorResume(this::handleApiError);

        } catch (Exception e) {
            log.error("Lỗi khi chuẩn bị request OpenAI: {}", e.getMessage());
            return Mono.just("Xin lỗi, tôi đang gặp sự cố kỹ thuật. Vui lòng thử lại sau.");
        }
    }

    /**
     * Trích xuất nội dung phản hồi từ OpenAI response
     */
    private String extractContentFromResponse(String responseBody) {
        try {
            JsonNode jsonResponse = objectMapper.readTree(responseBody);
            JsonNode choices = jsonResponse.path("choices");

            if (choices.isArray() && choices.size() > 0) {
                JsonNode firstChoice = choices.get(0);
                JsonNode message = firstChoice.path("message");
                return message.path("content").asText();
            }

            log.warn("Không thể trích xuất content từ OpenAI response: {}", responseBody);
            return "Xin lỗi, tôi không thể xử lý yêu cầu của bạn lúc này.";

        } catch (Exception e) {
            log.error("Lỗi khi parse OpenAI response: {}", e.getMessage());
            return "Xin lỗi, tôi đang gặp sự cố kỹ thuật. Vui lòng thử lại sau.";
        }
    }

    /**
     * Xử lý lỗi API
     */
    private Mono<String> handleApiError(Throwable error) {
        if (error instanceof WebClientResponseException) {
            WebClientResponseException webClientError = (WebClientResponseException) error;
            int statusCode = webClientError.getStatusCode().value();

            switch (statusCode) {
                case 401:
                    log.error("OpenAI API key không hợp lệ");
                    return Mono.just("Xin lỗi, hệ thống đang gặp sự cố xác thực. Vui lòng liên hệ admin.");
                case 429:
                    log.error("OpenAI API rate limit exceeded");
                    return Mono.just("Hệ thống đang quá tải, vui lòng thử lại sau ít phút.");
                case 500:
                case 502:
                case 503:
                    log.error("OpenAI server error: {}", statusCode);
                    return Mono.just("OpenAI đang gặp sự cố. Vui lòng thử lại sau.");
                default:
                    log.error("OpenAI API error {}: {}", statusCode, webClientError.getResponseBodyAsString());
                    return Mono.just("Xin lỗi, tôi đang gặp sự cố kỹ thuật. Vui lòng thử lại sau.");
            }
        }

        log.error("Unexpected error calling OpenAI: {}", error.getMessage());
        return Mono.just("Xin lỗi, tôi đang gặp sự cố kỹ thuật. Vui lòng thử lại sau.");
    }

    /**
     * Tạo system prompt cho chatbot nhà hàng
     */
    public String createRestaurantSystemPrompt(String restaurantContext) {
        return String.format("""
            Bạn là một trợ lý ảo thông minh của nhà hàng, có tên là "FoodBot".
            Bạn sinh ngày 05/3/2015 (trùng với ngày thành lập nhà hàng)
            Bạn được tạo ra bởi Duy Linh, chàng lập trình viên dễ thương!
            
            THÔNG TIN VỀ NHÀ HÀNG:
            %s
            
            VAI TRÒ VÀ NHIỆM VỤ:
            - Bạn là một nhân viên tư vấn chuyên nghiệp, thân thiện và hiểu biết sâu về thực đơn
            - Hỗ trợ khách hàng đặt món, tư vấn món ăn phù hợp
            - Trả lời các câu hỏi về thực đơn, giá cả, thời gian giao hàng
            - Hướng dẫn quy trình đặt hàng, thanh toán
            - Giải đáp thắc mắc về chính sách, khuyến mãi
            
            CÁCH THỨC GIAO TIẾP:
            - Luôn sử dụng tiếng Việt
            - Thân thiện, duyên dáng pha chút hài hước
            - Cách xưng hô đáp lại  tuỳ theo khách gọi bạn là gì (nếu khách gọi bạn là "bà" thì bạn xưng "tui" và cũng gọi khách là "bà", cách xưng hô linh hoạt theo kiểu miền Tây tuỳ theo tính cách khách hàng)
            - Sử dụng ngôn ngữ đơn giản, dễ hiểu
            - Tránh sử dụng biệt ngữ kỹ thuật hoặc từ ngữ phức tạp
            - Giữ cuộc trò chuyện ngắn gọn, tập trung vào câu hỏi của khách
            - Đưa ra câu trả lời cụ thể, hữu ích
            - Khi không biết thông tin, hãy thừa nhận và đề xuất liên hệ nhân viên
            - Có thể gợi ý món ăn dựa trên sở thích của khách
            - Luôn kết thúc bằng câu hỏi để tiếp tục hỗ trợ
            
            LƯU Ý QUAN TRỌNG:
            - Ưu tiên trả lời về các chủ đề liên quan đến nhà hàng và món ăn
            - Nếu được hỏi về chủ đề khác, bạn cũng tự nhiên trả lời nhưng nếu người dùng liên tiếp hỏi về chủ đề khác hãy lịch sự từ chối và chuyển hướng về dịch vụ nhà hàng
            - Luôn ưu tiên thông tin chính xác từ knowledge base được cung cấp
            """, restaurantContext != null ? restaurantContext : "Chưa có thông tin chi tiết về nhà hàng.");
    }
}
