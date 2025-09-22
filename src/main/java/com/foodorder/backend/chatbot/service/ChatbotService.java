package com.foodorder.backend.chatbot.service;

import com.foodorder.backend.chatbot.dto.ChatRequestDTO;
import com.foodorder.backend.chatbot.dto.ChatResponseDTO;
import com.foodorder.backend.chatbot.entity.ChatMessage;
import com.foodorder.backend.chatbot.repository.ChatMessageRepository;
import com.foodorder.backend.food.entity.Food;
import com.foodorder.backend.food.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service chính xử lý logic nghiệp vụ của Chatbot với tích hợp RAG và OpenAI
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotService {

    private final OpenAIService openAIService;
    private final RAGService ragService;
    private final ChatMessageRepository chatMessageRepository;
    private final FoodRepository foodRepository;

    @Value("${chatbot.context.max-history:10}")
    private Integer maxHistoryMessages;

    /**
     * Xử lý tin nhắn từ người dùng và trả về phản hồi từ chatbot
     */
    @Transactional
    public Mono<ChatResponseDTO> processMessage(ChatRequestDTO request) {
        long startTime = System.currentTimeMillis();

        return Mono.fromCallable(() -> {
//            log.info("Xử lý tin nhắn từ session: {}, user: {}", request.getSessionId(), request.getUserId());

            // Tạo session ID nếu chưa có
            String sessionId = request.getSessionId();
            if (sessionId == null || sessionId.isEmpty()) {
                sessionId = generateSessionId();
            }

            // Lưu tin nhắn của user
            ChatMessage userMessage = saveUserMessage(request, sessionId);

            // Lấy lịch sử hội thoại
            List<String> conversationHistory = getConversationHistory(sessionId);

            // Tìm context từ RAG
            String ragContext = ragService.retrieveRelevantContext(request.getMessage());

            // Tạo system prompt với thông tin nhà hàng
            String restaurantOverview = ragService.getRestaurantOverviewContext();
            String systemPrompt = openAIService.createRestaurantSystemPrompt(restaurantOverview);

            // Kết hợp system prompt với RAG context
            String enhancedPrompt = combinePromptWithRAG(systemPrompt, ragContext);

            return new ProcessingContext(sessionId, userMessage, conversationHistory,
                                       enhancedPrompt, ragContext, startTime);
        })
        .flatMap(context ->
            // Gọi OpenAI API
            openAIService.getChatCompletion(
                request.getMessage(),
                context.enhancedPrompt,
                context.conversationHistory
            )
            .map(aiResponse -> createChatResponse(context, aiResponse))
            .doOnNext(response ->
                // Lưu phản hồi của bot
                saveBotMessage(context.sessionId, response.getMessage(),
                             context.ragContext, response.getResponseTime())
            )
        )
        .doOnError(error -> log.error("Lỗi xử lý tin nhắn: {}", error.getMessage()))
        .onErrorResume(error ->
            Mono.just(createErrorResponse(request.getSessionId(), startTime))
        );
    }

    /**
     * Context class để truyền dữ liệu qua các bước xử lý
     */
    private static class ProcessingContext {
        final String sessionId;
        final ChatMessage userMessage;
        final List<String> conversationHistory;
        final String enhancedPrompt;
        final String ragContext;
        final long startTime;

        ProcessingContext(String sessionId, ChatMessage userMessage,
                         List<String> conversationHistory, String enhancedPrompt,
                         String ragContext, long startTime) {
            this.sessionId = sessionId;
            this.userMessage = userMessage;
            this.conversationHistory = conversationHistory;
            this.enhancedPrompt = enhancedPrompt;
            this.ragContext = ragContext;
            this.startTime = startTime;
        }
    }

    /**
     * Lưu tin nhắn của user vào database
     */
    private ChatMessage saveUserMessage(ChatRequestDTO request, String sessionId) {
        ChatMessage message = ChatMessage.builder()
                .sessionId(sessionId)
                .userId(request.getUserId())
                .messageType(ChatMessage.MessageType.USER)
                .messageContent(request.getMessage())
                .createdAt(LocalDateTime.now())
                .build();

        return chatMessageRepository.save(message);
    }

    /**
     * Lưu tin nhắn của bot vào database
     */
    private void saveBotMessage(String sessionId, String content, String contextUsed, Integer responseTime) {
        try {
            ChatMessage message = ChatMessage.builder()
                    .sessionId(sessionId)
                    .messageType(ChatMessage.MessageType.BOT)
                    .messageContent(content)
                    .contextUsed(contextUsed)
                    .responseTime(responseTime)
                    .createdAt(LocalDateTime.now())
                    .build();

            chatMessageRepository.save(message);
        } catch (Exception e) {
            log.error("Lỗi khi lưu tin nhắn bot: {}", e.getMessage());
        }
    }

    /**
     * Lấy lịch sử hội thoại gần đây
     */
    private List<String> getConversationHistory(String sessionId) {
        try {
            List<ChatMessage> recentMessages = chatMessageRepository
                .findRecentMessagesBySessionId(sessionId, PageRequest.of(0, maxHistoryMessages));

            return recentMessages.stream()
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt))
                .map(msg -> msg.getMessageType().name() + ": " + msg.getMessageContent())
                .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Lỗi khi lấy lịch sử hội thoại: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Kết hợp system prompt với RAG context
     */
    private String combinePromptWithRAG(String systemPrompt, String ragContext) {
        if (ragContext == null || ragContext.trim().isEmpty()) {
            return systemPrompt;
        }

        return systemPrompt + "\n\n" + ragContext;
    }

    /**
     * Tạo ChatResponse từ kết quả xử lý
     */
    private ChatResponseDTO createChatResponse(ProcessingContext context, String aiResponse) {
        int responseTime = (int) (System.currentTimeMillis() - context.startTime);

        // Tạo suggestions dựa trên nội dung phản hồi
        List<String> suggestions = generateSuggestions(aiResponse, context.userMessage.getMessageContent());

        // Tạo quick actions
        List<ChatResponseDTO.QuickActionDTO> quickActions = generateQuickActions(aiResponse);

        // Kiểm tra xem có nên gợi ý sản phẩm không
        ChatResponseDTO.RecommendationDataDTO recommendations =
            generateFoodRecommendations(context.userMessage.getMessageContent());

        return ChatResponseDTO.builder()
                .sessionId(context.sessionId)
                .message(aiResponse)
                .messageType("text")
                .timestamp(LocalDateTime.now())
                .responseTime(responseTime)
                .suggestions(suggestions)
                .quickActions(quickActions)
                .recommendationData(recommendations)
                .isFromKnowledgeBase(context.ragContext != null && !context.ragContext.isEmpty())
                .confidenceScore(calculateConfidenceScore(context.ragContext, aiResponse))
                .build();
    }

    /**
     * Tạo response khi có lỗi
     */
    private ChatResponseDTO createErrorResponse(String sessionId, long startTime) {
        int responseTime = (int) (System.currentTimeMillis() - startTime);

        return ChatResponseDTO.builder()
                .sessionId(sessionId)
                .message("Xin lỗi, tôi đang gặp sự cố kỹ thuật. Vui lòng thử lại sau hoặc liên hệ nhân viên hỗ trợ.")
                .messageType("error")
                .timestamp(LocalDateTime.now())
                .responseTime(responseTime)
                .suggestions(Arrays.asList(
                    "Liên hệ hotline hỗ trợ",
                    "Xem thực đơn",
                    "Thử lại câu hỏi"
                ))
                .isFromKnowledgeBase(false)
                .confidenceScore(0.0)
                .build();
    }

    /**
     * Tạo gợi ý câu hỏi tiếp theo
     */
    private List<String> generateSuggestions(String botResponse, String userMessage) {
        List<String> suggestions = new ArrayList<>();

        String lowerResponse = botResponse.toLowerCase();
        String lowerUserMessage = userMessage.toLowerCase();

        // Gợi ý dựa trên nội dung phản hồi
        if (lowerResponse.contains("thực đơn") || lowerResponse.contains("menu")) {
            suggestions.add("Món nào phổ biến nhất?");
            suggestions.add("Có món chay không?");
        }

        if (lowerResponse.contains("giá") || lowerResponse.contains("tiền")) {
            suggestions.add("Có khuyến mãi gì không?");
            suggestions.add("Phí giao hàng bao nhiêu?");
        }

        if (lowerResponse.contains("giao hàng") || lowerResponse.contains("ship")) {
            suggestions.add("Giao hàng mất bao lâu?");
            suggestions.add("Khu vực giao hàng ra sao?");
        }

        // Gợi ý chung
        if (suggestions.isEmpty()) {
            suggestions.addAll(Arrays.asList(
                "Xem thực đơn",
                "Thông tin giao hàng",
                "Khuyến mãi hiện tại",
                "Cách đặt hàng"
            ));
        }

        return suggestions.stream().limit(4).collect(Collectors.toList());
    }

    /**
     * Tạo quick actions
     */
    private List<ChatResponseDTO.QuickActionDTO> generateQuickActions(String botResponse) {
        List<ChatResponseDTO.QuickActionDTO> actions = new ArrayList<>();

        String lowerResponse = botResponse.toLowerCase();

        if (lowerResponse.contains("thực đơn") || lowerResponse.contains("menu")) {
            actions.add(ChatResponseDTO.QuickActionDTO.builder()
                .label("Xem Thực Đơn")
                .action("view_menu")
                .url("/menu")
                .build());
        }

        if (lowerResponse.contains("đặt hàng") || lowerResponse.contains("order")) {
            actions.add(ChatResponseDTO.QuickActionDTO.builder()
                .label("Đặt Hàng Ngay")
                .action("place_order")
                .url("/order")
                .build());
        }

        if (lowerResponse.contains("liên hệ") || lowerResponse.contains("hỗ trợ")) {
            actions.add(ChatResponseDTO.QuickActionDTO.builder()
                .label("Liên Hệ Hỗ Trợ")
                .action("contact_support")
                .url("/contact")
                .build());
        }

        return actions;
    }

    /**
     * Tạo gợi ý món ăn dựa trên tin nhắn user
     */
    private ChatResponseDTO.RecommendationDataDTO generateFoodRecommendations(String userMessage) {
        try {
            String lowerMessage = userMessage.toLowerCase();

            // Chỉ gợi ý khi user hỏi về món ăn
            if (!lowerMessage.contains("món") && !lowerMessage.contains("ăn") &&
                !lowerMessage.contains("thực đơn") && !lowerMessage.contains("menu")) {
                return null;
            }

            // Lấy một số món ăn mới nhất (dựa theo ID thay vì createdAt)
            List<Food> popularFoods = foodRepository.findTop6ByOrderByIdDesc();

            if (popularFoods.isEmpty()) {
                return null;
            }

            List<ChatResponseDTO.RecommendationDataDTO.ProductRecommendationDTO> recommendations =
                popularFoods.stream().limit(4).map(food ->
                    ChatResponseDTO.RecommendationDataDTO.ProductRecommendationDTO.builder()
                        .id(food.getId())
                        .name(food.getName())
                        .description(food.getDescription())
                        .price(food.getPrice().doubleValue()) // Sửa lỗi chuyển đổi từ BigDecimal sang Double
                        .imageUrl(food.getImageUrl())
                        .category(food.getCategory() != null ? food.getCategory().getName() : "")
                        .rating(4.5) // Default rating
                        .build()
                ).collect(Collectors.toList());

            return ChatResponseDTO.RecommendationDataDTO.builder()
                .foods(recommendations)
                .reason("Đây là những món ăn mới nhất mà bạn có thể quan tâm")
                .build();

        } catch (Exception e) {
            log.error("Lỗi khi tạo food recommendations: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Tính điểm tin cậy của câu trả lời
     */
    private Double calculateConfidenceScore(String ragContext, String aiResponse) {
        if (ragContext == null || ragContext.isEmpty()) {
            return 0.6; // Điểm thấp hơn khi không có context từ RAG
        }

        if (aiResponse.toLowerCase().contains("xin lỗi") ||
            aiResponse.toLowerCase().contains("không biết")) {
            return 0.4;
        }

        return 0.9; // Điểm cao khi có RAG context và phản hồi tích cực
    }

    /**
     * Tạo session ID ngẫu nhiên
     */
    private String generateSessionId() {
        return "session_" + System.currentTimeMillis() + "_" +
               UUID.randomUUID().toString().substring(0, 8);
    }

    /**
     * Đánh giá phản hồi của bot
     */
    @Transactional
    public boolean rateResponse(String sessionId, Long messageId, Integer rating) {
        try {
            if (rating < 1 || rating > 5) {
                return false;
            }

            Optional<ChatMessage> messageOpt = chatMessageRepository.findById(messageId);
            if (messageOpt.isPresent()) {
                ChatMessage message = messageOpt.get();
                if (message.getSessionId().equals(sessionId) &&
                    message.getMessageType() == ChatMessage.MessageType.BOT) {
                    message.setUserRating(rating);
                    chatMessageRepository.save(message);
                    return true;
                }
            }
            return false;

        } catch (Exception e) {
            log.error("Lỗi khi đánh giá phản hồi: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Lấy lịch sử chat của session
     */
    public List<ChatMessage> getChatHistory(String sessionId) {
        return chatMessageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
    }
}
