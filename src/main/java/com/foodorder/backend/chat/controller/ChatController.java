package com.foodorder.backend.chat.controller;

import com.foodorder.backend.chat.dto.ChatMessageResponse;
import com.foodorder.backend.chat.service.ChatService;
import com.foodorder.backend.security.JwtUtil;
import com.foodorder.backend.user.entity.User;
import com.foodorder.backend.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller cho Chat Management
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * Lấy lịch sử chat của user hiện tại
     */
    @GetMapping("/history")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getChatHistory(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size,
                                          HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            Pageable pageable = PageRequest.of(page, size);

            Page<ChatMessageResponse> chatHistory = chatService.getChatHistoryForUserPageable(currentUser, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("messages", chatHistory.getContent());
            response.put("currentPage", chatHistory.getNumber());
            response.put("totalPages", chatHistory.getTotalPages());
            response.put("totalElements", chatHistory.getTotalElements());
            response.put("hasNext", chatHistory.hasNext());
            response.put("hasPrevious", chatHistory.hasPrevious());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy lịch sử chat: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "errorCode", "CHAT_HISTORY_ERROR",
                "message", "Lỗi khi lấy lịch sử chat"
            ));
        }
    }

    /**
     * Lấy tin nhắn chưa đọc của user hiện tại
     */
    @GetMapping("/unread")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getUnreadMessages(HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            List<ChatMessageResponse> unreadMessages = chatService.getUnreadMessagesForUser(currentUser);

            return ResponseEntity.ok(Map.of(
                "unreadMessages", unreadMessages,
                "count", unreadMessages.size()
            ));

        } catch (Exception e) {
            log.error("Lỗi khi lấy tin nhắn chưa đọc: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "errorCode", "UNREAD_MESSAGES_ERROR",
                "message", "Lỗi khi lấy tin nhắn chưa đọc"
            ));
        }
    }

    /**
     * Đánh dấu tin nhắn đã đọc
     */
    @PutMapping("/mark-read/{messageId}")
    @PreAuthorize("hasAnyRole('USER', 'STAFF', 'ADMIN')")
    public ResponseEntity<?> markMessageAsRead(@PathVariable String messageId,
                                             HttpServletRequest request) {
        try {
            chatService.markMessageAsRead(messageId);

            return ResponseEntity.ok(Map.of(
                "message", "Đã đánh dấu tin nhắn là đã đọc",
                "messageId", messageId
            ));

        } catch (Exception e) {
            log.error("Lỗi khi đánh dấu tin nhắn đã đọc: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "errorCode", "MARK_READ_ERROR",
                "message", "Lỗi khi đánh dấu tin nhắn đã đọc"
            ));
        }
    }

    // ========== STAFF/ADMIN ENDPOINTS ==========

    /**
     * Lấy tất cả tin nhắn từ user gửi cho staff
     */
    @GetMapping("/staff/all-messages")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<?> getAllUserToStaffMessages(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size,
                                                     HttpServletRequest request) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<ChatMessageResponse> messages = chatService.getAllUserToStaffMessages(pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("messages", messages.getContent());
            response.put("currentPage", messages.getNumber());
            response.put("totalPages", messages.getTotalPages());
            response.put("totalElements", messages.getTotalElements());
            response.put("hasNext", messages.hasNext());
            response.put("hasPrevious", messages.hasPrevious());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy tin nhắn cho staff: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "errorCode", "STAFF_MESSAGES_ERROR",
                "message", "Lỗi khi lấy tin nhắn"
            ));
        }
    }

    /**
     * Lấy tin nhắn từ user cụ thể
     */
    @GetMapping("/staff/user/{userId}/messages")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<?> getUserMessages(@PathVariable Long userId,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "20") int size,
                                           HttpServletRequest request) {
        try {
            User user = userService.findById(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "errorCode", "USER_NOT_FOUND",
                    "message", "Không tìm thấy người dùng"
                ));
            }

            // Sử dụng conversation ID thay vì user để lấy tin nhắn cho staff
            // Tìm conversation của user này trước
            try {
                // Giả sử có ConversationService để lấy conversation
                // Page<ChatMessageResponse> messages = chatService.getChatHistoryForStaffInConversationPageable(conversationId, pageable);

                // Tạm thời sử dụng method cũ cho đến khi implement đầy đủ ConversationService
                Pageable pageable = PageRequest.of(page, size);
                List<ChatMessageResponse> allMessages = chatService.getChatHistoryForUser(user);

                // Convert List to Page manually (temporary solution)
                int start = (int) pageable.getOffset();
                int end = Math.min((start + pageable.getPageSize()), allMessages.size());
                List<ChatMessageResponse> pageContent = allMessages.subList(start, end);

                Map<String, Object> response = new HashMap<>();
                response.put("messages", pageContent);
                response.put("currentPage", page);
                response.put("totalPages", (int) Math.ceil((double) allMessages.size() / size));
                response.put("totalElements", allMessages.size());
                response.put("hasNext", end < allMessages.size());
                response.put("hasPrevious", start > 0);
                response.put("user", Map.of(
                    "id", user.getId(),
                    "name", user.getFullName(),
                    "email", user.getEmail(),
                    "phone", user.getPhoneNumber()
                ));

                return ResponseEntity.ok(response);

            } catch (Exception conversationError) {
                log.warn("Không thể lấy tin nhắn qua conversation, sử dụng phương pháp fallback: {}", conversationError.getMessage());

                // Fallback: trả về empty result
                Map<String, Object> response = new HashMap<>();
                response.put("messages", List.of());
                response.put("currentPage", 0);
                response.put("totalPages", 0);
                response.put("totalElements", 0);
                response.put("hasNext", false);
                response.put("hasPrevious", false);
                response.put("user", Map.of(
                    "id", user.getId(),
                    "name", user.getFullName(),
                    "email", user.getEmail(),
                    "phone", user.getPhoneNumber()
                ));

                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            log.error("Lỗi khi lấy tin nhắn của user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "errorCode", "USER_MESSAGES_ERROR",
                "message", "Lỗi khi lấy tin nhắn của người dùng"
            ));
        }
    }

    /**
     * Kiểm tra xem tin nhắn từ khách hàng cụ thể đã được đọc hết hay chưa và con số cụ thể
     */
    @GetMapping("/staff/user/{userId}/read-status")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<?> getUserMessageReadStatus(@PathVariable Long userId,
                                                    HttpServletRequest request) {
        try {
            User user = userService.findById(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "errorCode", "USER_NOT_FOUND",
                    "message", "Không tìm thấy người dùng"
                ));
            }

            // Đếm số tin nhắn chưa đọc từ user này mà staff chưa đọc (logic đúng)
            Long unreadCount = chatService.countUnreadMessagesFromUserForStaff(user);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("userName", user.getFullName());
            response.put("userEmail", user.getEmail());
            response.put("unreadCount", unreadCount); // Số tin nhắn chưa đọc cụ thể
            response.put("hasUnreadMessages", unreadCount > 0); // Boolean để FE dễ check

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Lỗi khi lấy số tin nhắn chưa đọc của user {}: {}", userId, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "errorCode", "UNREAD_COUNT_ERROR",
                "message", "Lỗi khi lấy số tin nhắn chưa đọc"
            ));
        }
    }

    /**
     * Lấy số tin nhắn chưa đọc từ tất cả user
     */
    @GetMapping("/staff/unread-count")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<?> getUnreadCount(HttpServletRequest request) {
        try {
            Long unreadCount = chatService.countUnreadUserToStaffMessages();

            return ResponseEntity.ok(Map.of(
                "unreadCount", unreadCount
            ));

        } catch (Exception e) {
            log.error("Lỗi khi lấy số tin nhắn chưa đọc: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "errorCode", "UNREAD_COUNT_ERROR",
                "message", "Lỗi khi lấy số tin nhắn chưa đọc"
            ));
        }
    }

    /**
     * Lấy danh sách user đã chat với staff
     */
    @GetMapping("/staff/users")
    @PreAuthorize("hasAnyRole('STAFF', 'ADMIN')")
    public ResponseEntity<?> getUsersChatWithStaff(HttpServletRequest request) {
        try {
            List<User> users = chatService.getUsersChatWithStaff();

            // Tạo response với thông tin cần thiết và số tin nhắn chưa đọc cho mỗi user
            List<Map<String, Object>> userList = users.stream().map(user -> {
                // Sử dụng method đúng để đếm tin nhắn user gửi cho staff mà staff chưa đọc
                Long unreadCount = chatService.countUnreadMessagesFromUserForStaff(user);

                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("fullName", user.getFullName());
                userInfo.put("email", user.getEmail());
                userInfo.put("phoneNumber", user.getPhoneNumber());
                userInfo.put("unreadCount", unreadCount);
                userInfo.put("hasUnreadMessages", unreadCount > 0);

                return userInfo;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                "users", userList,
                "totalUsers", userList.size()
            ));

        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách user đã chat: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "errorCode", "USERS_CHAT_ERROR",
                "message", "Lỗi khi lấy danh sách user đã chat"
            ));
        }
    }

    // ========== ADMIN ONLY ENDPOINTS ==========

    /**
     * Lấy thống kê chat trong khoảng thời gian
     */
    @GetMapping("/admin/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getChatStatistics(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
                                             HttpServletRequest request) {
        try {
            List<ChatMessageResponse> messages = chatService.getMessagesBetweenDates(startDate, endDate);

            long userToStaffCount = messages.stream()
                    .filter(msg -> "USER_TO_STAFF".equals(msg.getMessageType()))
                    .count();

            long staffToUserCount = messages.stream()
                    .filter(msg -> "STAFF_TO_USER".equals(msg.getMessageType()))
                    .count();

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalMessages", messages.size());
            statistics.put("userToStaffMessages", userToStaffCount);
            statistics.put("staffToUserMessages", staffToUserCount);
            statistics.put("startDate", startDate);
            statistics.put("endDate", endDate);
            statistics.put("messages", messages);

            return ResponseEntity.ok(statistics);

        } catch (Exception e) {
            log.error("Lỗi khi lấy thống kê chat: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "errorCode", "STATISTICS_ERROR",
                "message", "Lỗi khi lấy thống kê chat"
            ));
        }
    }

    // ========== HELPER METHODS ==========

    private User getCurrentUser(HttpServletRequest request) {
        String token = jwtUtil.getTokenFromRequest(request);
        if (token != null && jwtUtil.validateToken(token)) {
            String username = jwtUtil.getUsernameFromToken(token);
            return userService.findByUsername(username);
        }
        throw new RuntimeException("Không thể xác thực người dùng");
    }
}
