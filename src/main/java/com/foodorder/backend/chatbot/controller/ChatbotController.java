package com.foodorder.backend.chatbot.controller;

import com.foodorder.backend.chatbot.dto.ChatRequestDTO;
import com.foodorder.backend.chatbot.entity.ChatbotMessage;
import com.foodorder.backend.chatbot.service.ChatbotService;
import com.foodorder.backend.exception.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * Controller xử lý API cho Chatbot AI
 */
@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
@Tag(name = "Chatbot", description = "API Chatbot AI hỗ trợ khách hàng")
public class ChatbotController {

    private final ChatbotService chatbotService;

    @Operation(summary = "Chat với bot", description = "Gửi tin nhắn và nhận phản hồi từ AI chatbot.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ"),
            @ApiResponse(responseCode = "500", description = "Lỗi server")
    })
    @PostMapping("/chat")
    public Mono<ResponseEntity<Object>> chat(
            @Valid @RequestBody ChatRequestDTO request,
            @Parameter(hidden = true) Authentication authentication) {
        try {
            // Lấy user ID nếu đã đăng nhập
            if (authentication != null && request.getUserId() == null) {
                log.debug("User đã đăng nhập nhưng chưa set userId trong request");
            }

            log.info("Nhận tin nhắn chat: {}", request.getMessage());

            return chatbotService.processMessage(request)
                .map(response -> ResponseEntity.ok().body((Object) response))
                .onErrorResume(error -> {
                    log.error("Lỗi trong chat API: {}", error.getMessage(), error);
                    ApiError apiError = ApiError.builder()
                        .errorCode("CHATBOT_ERROR")
                        .message("Lỗi khi xử lý tin nhắn chat")
                        .details(error.getMessage())
                        .build();
                    return Mono.just(ResponseEntity.internalServerError().body((Object) apiError));
                });

        } catch (Exception e) {
            log.error("Lỗi validation trong chat API: {}", e.getMessage(), e);
            ApiError apiError = ApiError.builder()
                .errorCode("INVALID_REQUEST")
                .message("Yêu cầu không hợp lệ")
                .details(e.getMessage())
                .build();
            return Mono.just(ResponseEntity.badRequest().body((Object) apiError));
        }
    }

    @Operation(summary = "Lịch sử chat", description = "Lấy lịch sử chat theo session ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "400", description = "Session ID không hợp lệ")
    })
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<?> getChatHistory(
            @Parameter(description = "Session ID của cuộc hội thoại") @PathVariable String sessionId) {
        try {
            if (sessionId == null || sessionId.trim().isEmpty()) {
                ApiError apiError = ApiError.builder()
                    .errorCode("INVALID_SESSION_ID")
                    .message("Session ID không hợp lệ")
                    .build();
                return ResponseEntity.badRequest().body(apiError);
            }

            List<ChatbotMessage> history = chatbotService.getChatHistory(sessionId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", history
            ));

        } catch (Exception e) {
            log.error("Lỗi khi lấy lịch sử chat: {}", e.getMessage(), e);
            ApiError apiError = ApiError.builder()
                .errorCode("HISTORY_FETCH_ERROR")
                .message("Không thể lấy lịch sử chat")
                .details(e.getMessage())
                .build();
            return ResponseEntity.internalServerError().body(apiError);
        }
    }

    @Operation(summary = "Đánh giá phản hồi", description = "Đánh giá chất lượng phản hồi của chatbot (1-5 sao).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Đánh giá thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    })
    @PostMapping("/rate")
    public ResponseEntity<?> rateResponse(@RequestBody Map<String, Object> request) {
        try {
            String sessionId = (String) request.get("sessionId");
            Object messageIdObj = request.get("messageId");
            Object ratingObj = request.get("rating");

            if (sessionId == null || messageIdObj == null || ratingObj == null) {
                ApiError apiError = ApiError.builder()
                    .errorCode("MISSING_PARAMETERS")
                    .message("Thiếu thông tin bắt buộc")
                    .details("Cần có sessionId, messageId và rating")
                    .build();
                return ResponseEntity.badRequest().body(apiError);
            }

            Long messageId = Long.valueOf(messageIdObj.toString());
            Integer rating = Integer.valueOf(ratingObj.toString());

            if (rating < 1 || rating > 5) {
                ApiError apiError = ApiError.builder()
                    .errorCode("INVALID_RATING")
                    .message("Rating phải từ 1 đến 5")
                    .build();
                return ResponseEntity.badRequest().body(apiError);
            }

            boolean success = chatbotService.rateResponse(sessionId, messageId, rating);

            if (success) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đánh giá thành công"
                ));
            } else {
                ApiError apiError = ApiError.builder()
                    .errorCode("RATING_FAILED")
                    .message("Không thể đánh giá phản hồi")
                    .details("Tin nhắn không tồn tại hoặc không thuộc session này")
                    .build();
                return ResponseEntity.badRequest().body(apiError);
            }

        } catch (NumberFormatException e) {
            log.error("Lỗi format số trong rate response: {}", e.getMessage());
            ApiError apiError = ApiError.builder()
                .errorCode("INVALID_NUMBER_FORMAT")
                .message("Định dạng số không hợp lệ")
                .details("messageId và rating phải là số hợp lệ")
                .build();
            return ResponseEntity.badRequest().body(apiError);
        } catch (Exception e) {
            log.error("Lỗi khi đánh giá phản hồi: {}", e.getMessage(), e);
            ApiError apiError = ApiError.builder()
                .errorCode("RATING_ERROR")
                .message("Lỗi khi đánh giá phản hồi")
                .details(e.getMessage())
                .build();
            return ResponseEntity.internalServerError().body(apiError);
        }
    }

    @Operation(summary = "Health check", description = "Kiểm tra trạng thái hoạt động của chatbot service.")
    @ApiResponse(responseCode = "200", description = "Service đang hoạt động")
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "service", "chatbot",
            "timestamp", System.currentTimeMillis(),
            "version", "1.0.0"
        ));
    }

}
