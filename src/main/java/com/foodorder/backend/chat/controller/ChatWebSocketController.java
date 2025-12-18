package com.foodorder.backend.chat.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodorder.backend.chat.dto.ChatMessageRequest;
import com.foodorder.backend.chat.dto.ChatMessageResponse;
import com.foodorder.backend.chat.dto.ChatRegistrationRequest;
import com.foodorder.backend.chat.entity.ChatMessage;
import com.foodorder.backend.chat.service.ChatService;
import com.foodorder.backend.security.JwtUtil;
import com.foodorder.backend.user.entity.User;
import com.foodorder.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket Controller chuyên xử lý Chat giữa User và Staff
 * Hỗ trợ quản lý trạng thái online/offline và load tin nhắn chưa đọc
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    // Quản lý staff đang online (staffId -> sessionId)
    private final Map<String, String> onlineStaff = new ConcurrentHashMap<>();

    // Quản lý user đang online (userId -> sessionId)
    private final Map<String, String> onlineUsers = new ConcurrentHashMap<>();

    /**
     * User gửi tin nhắn tới Staff (hỗ trợ cả tin nhắn thường và reply tin nhắn cụ thể)
     */
    @MessageMapping("/chat/user-to-staff")
    public void handleUserToStaffMessage(@Payload Object payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            // Parse payload thành ChatMessageRequest
            ChatMessageRequest request = parsePayloadToChatRequest(payload);

            // Validate request
            chatService.validateChatMessageRequest(request);

            // Validate JWT token
            if (!jwtUtil.validateToken(request.getToken())) {
                sendErrorToUser("INVALID_TOKEN", "Phiên đăng nhập đã hết hạn", headerAccessor.getSessionId());
                return;
            }

            // Lấy thông tin user từ token
            String username = jwtUtil.getUsernameFromToken(request.getToken());
            User user = userService.findByUsername(username);

            if (user == null) {
                sendErrorToUser("USER_NOT_FOUND", "Không tìm thấy thông tin người dùng", headerAccessor.getSessionId());
                return;
            }

            // Kiểm tra user phải là ROLE_USER
            if (!"ROLE_USER".equals(user.getRole().getCode())) {
                sendErrorToUser("ACCESS_DENIED", "Chỉ khách hàng mới có thể gửi tin nhắn hỗ trợ",
                        headerAccessor.getSessionId());
                return;
            }

            // Lưu thông tin user vào session và online map
            String userId = user.getId().toString();
            saveUserInfoToSession(headerAccessor, user);
            onlineUsers.put(userId, headerAccessor.getSessionId());

            // Tạo messageId
            String messageId = UUID.randomUUID().toString();
            ChatMessage savedMessage;

            // Kiểm tra có phải reply tin nhắn cụ thể không
            boolean isReply = request.getReplyToMessageId() != null && !request.getReplyToMessageId().trim().isEmpty();

            if (isReply) {
                // Phản hồi tin nhắn cụ thể của Staff
                ChatMessage originalMessage = chatService.findMessageById(request.getReplyToMessageId());
                if (originalMessage == null) {
                    sendErrorToUser("ORIGINAL_MESSAGE_NOT_FOUND", "Không tìm thấy tin nhắn gốc để phản hồi",
                            headerAccessor.getSessionId());
                    return;
                }

                savedMessage = chatService.sendUserReplyMessage(
                        user,
                        request.getMessage(),
                        messageId,
                        request.getReplyToMessageId(),
                        headerAccessor.getSessionId());

                // Gửi tin nhắn reply tới staff (kèm thông tin tin nhắn gốc)
                if (hasOnlineStaff()) {
                    sendUserReplyMessageToOnlineStaff(savedMessage, originalMessage);
                    log.info("Tin nhắn reply từ user {} đã được gửi tới staff online", userId);
                } else {
                    sendReplyConfirmationToUser(userId, savedMessage, originalMessage);
                    log.info("Không có staff online, tin nhắn reply từ user {} sẽ được load khi staff online", userId);
                }

                log.info("User {} đã phản hồi tin nhắn {} thành công", userId, request.getReplyToMessageId());

            } else {
                // Gửi tin nhắn thường (không reply)
                savedMessage = chatService.sendUserToStaffMessage(
                        user,
                        request.getMessage(),
                        messageId,
                        headerAccessor.getSessionId());

                // Kiểm tra staff online và gửi tin nhắn
                if (hasOnlineStaff()) {
                    sendMessageToOnlineStaff(savedMessage);
                    log.info("Tin nhắn từ user {} đã được gửi tới staff online", userId);
                } else {
                    sendConfirmationToUser(userId, savedMessage);
                    log.info("Không có staff online, tin nhắn từ user {} sẽ được load khi staff online", userId);
                }

                log.info("User {} đã gửi tin nhắn chat tới staff: {}", userId, request.getMessage());
            }

        } catch (Exception e) {
            log.error("Lỗi khi xử lý tin nhắn từ user tới staff: {}", e.getMessage());
            sendErrorToUser("CHAT_ERROR", "Lỗi khi gửi tin nhắn: " + e.getMessage(), headerAccessor.getSessionId());
        }
    }

    /**
     * Staff gửi tin nhắn trả lời cho User (chỉ có 1 staff duy nhất)
     */
    @MessageMapping("/chat/staff-to-user")
    public void handleStaffToUserMessage(@Payload Object payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            // Parse payload thành ChatMessageRequest
            ChatMessageRequest request = parsePayloadToChatRequest(payload);

            // Validate request
            chatService.validateChatMessageRequest(request);

            // Validate JWT token
            if (!jwtUtil.validateToken(request.getToken())) {
                sendErrorToStaff("INVALID_TOKEN", "Phiên đăng nhập đã hết hạn", headerAccessor.getSessionId());
                return;
            }

            // Lấy thông tin staff từ token
            String username = jwtUtil.getUsernameFromToken(request.getToken());
            User staff = userService.findByUsername(username);

            if (staff == null) {
                sendErrorToStaff("STAFF_NOT_FOUND", "Không tìm thấy thông tin nhân viên",
                        headerAccessor.getSessionId());
                return;
            }

            // Kiểm tra phải là staff hoặc admin
            String roleCode = staff.getRole().getCode();
            if (!"ROLE_STAFF".equals(roleCode) && !"ROLE_ADMIN".equals(roleCode)) {
                sendErrorToStaff("ACCESS_DENIED", "Chỉ nhân viên mới có thể trả lời tin nhắn",
                        headerAccessor.getSessionId());
                return;
            }

            // Lưu staff vào online map
            String staffId = staff.getId().toString();
            onlineStaff.put(staffId, headerAccessor.getSessionId());

            // Lưu tin nhắn vào DB - staff broadcast cho tất cả user hoặc trả lời user cụ
            // thể
            String messageId = UUID.randomUUID().toString();
            ChatMessage savedMessage = chatService.sendStaffBroadcastMessage(
                    staff,
                    request.getMessage(),
                    messageId);

            // Gửi tin nhắn tới tất cả user online
            broadcastStaffMessageToAllUsers(savedMessage);

            // Gửi xác nhận tới staff
            sendConfirmationToStaff(savedMessage, onlineUsers.size() > 0);

            log.info("Staff {} đã gửi tin nhắn broadcast thành công", staffId);

        } catch (Exception e) {
            log.error("Lỗi khi xử lý tin nhắn từ staff: {}", e.getMessage());
            sendErrorToStaff("CHAT_ERROR", "Lỗi khi gửi tin nhắn: " + e.getMessage(), headerAccessor.getSessionId());
        }
    }

    /**
     * User đăng ký nhận tin nhắn chat
     */
    @MessageMapping("/chat/user/register")
    public void registerUserForChat(@Payload Object payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            ChatRegistrationRequest request = parsePayloadToRegistrationRequest(payload);

            if (!jwtUtil.validateToken(request.getToken())) {
                sendErrorToUser("INVALID_TOKEN", "Phiên đăng nhập đã hết hạn", headerAccessor.getSessionId());
                return;
            }

            String username = jwtUtil.getUsernameFromToken(request.getToken());
            User user = userService.findByUsername(username);

            if (user == null) {
                sendErrorToUser("USER_NOT_FOUND", "Không tìm thấy thông tin người dùng", headerAccessor.getSessionId());
                return;
            }

            String userId = user.getId().toString();
            saveUserInfoToSession(headerAccessor, user);

            // Đánh dấu user là online
            onlineUsers.put(userId, headerAccessor.getSessionId());

            // Load tin nhắn chưa đọc khi user online
            loadUnreadMessagesForUser(userId, user);

            // Gửi thông báo welcome
            Map<String, Object> welcomeMessage = new HashMap<>();
            welcomeMessage.put("type", "CHAT_WELCOME");
            welcomeMessage.put("message", "Xin chào " + user.getFullName()
                    + "! Bạn có thể gửi tin nhắn và nhận phản hồi từ nhân viên hỗ trợ.");
            welcomeMessage.put("userId", userId);
            welcomeMessage.put("onlineStaffCount", onlineStaff.size());
            welcomeMessage.put("timestamp", LocalDateTime.now().toString());

            messagingTemplate.convertAndSendToUser(userId, "/queue/chat-messages", welcomeMessage);

            log.info("User {} đã online và đăng ký chat thành công", userId);

        } catch (Exception e) {
            log.error("Lỗi khi đăng ký user chat: {}", e.getMessage());
            sendErrorToUser("REGISTRATION_ERROR", "Lỗi đăng ký chat: " + e.getMessage(), headerAccessor.getSessionId());
        }
    }

    /**
     * Staff đăng ký nhận tin nhắn chat
     */
    @MessageMapping("/chat/staff/register")
    public void registerStaffForChat(@Payload Object payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            ChatRegistrationRequest request = parsePayloadToRegistrationRequest(payload);

            if (!jwtUtil.validateToken(request.getToken())) {
                sendErrorToStaff("INVALID_TOKEN", "Phiên đăng nhập đã hết hạn", headerAccessor.getSessionId());
                return;
            }

            String username = jwtUtil.getUsernameFromToken(request.getToken());
            User staff = userService.findByUsername(username);

            if (staff == null) {
                sendErrorToStaff("STAFF_NOT_FOUND", "Không tìm thấy thông tin nhân viên",
                        headerAccessor.getSessionId());
                return;
            }

            String roleCode = staff.getRole().getCode();
            if (!"ROLE_STAFF".equals(roleCode) && !"ROLE_ADMIN".equals(roleCode)) {
                sendErrorToStaff("ACCESS_DENIED", "Chỉ nhân viên mới có thể sử dụng tính năng này",
                        headerAccessor.getSessionId());
                return;
            }

            String staffId = staff.getId().toString();
            Map<String, Object> sessionAttrs = headerAccessor.getSessionAttributes();
            if (sessionAttrs != null) {
                sessionAttrs.put("staffId", staffId);
                sessionAttrs.put("staffName", staff.getFullName());
            }

            // Đánh dấu staff là online
            onlineStaff.put(staffId, headerAccessor.getSessionId());

            // Load tất cả tin nhắn chưa đọc khi staff online
            loadUnreadMessagesForStaff(staffId);

            // Gửi thông báo welcome và thống kê
            Long unreadCount = chatService.countUnreadUserToStaffMessages();
            int onlineUserCount = onlineUsers.size();

            Map<String, Object> welcomeMessage = new HashMap<>();
            welcomeMessage.put("type", "STAFF_CHAT_WELCOME");
            welcomeMessage.put("message",
                    "Xin chào " + staff.getFullName() + "! Bạn đã online và sẵn sàng nhận tin nhắn từ khách hàng.");
            welcomeMessage.put("staffId", staffId);
            welcomeMessage.put("unreadCount", unreadCount);
            welcomeMessage.put("onlineUserCount", onlineUserCount);
            welcomeMessage.put("timestamp", LocalDateTime.now().toString());

            // Gửi tới staff vừa online
            messagingTemplate.convertAndSendToUser(staffId, "/queue/staff-chat", welcomeMessage);

            // Thông báo tới tất cả staff khác rằng có staff mới online
            Map<String, Object> staffOnlineNotification = new HashMap<>();
            staffOnlineNotification.put("type", "STAFF_ONLINE");
            staffOnlineNotification.put("staffId", staffId);
            staffOnlineNotification.put("staffName", staff.getFullName());
            staffOnlineNotification.put("totalOnlineStaff", onlineStaff.size());
            staffOnlineNotification.put("timestamp", LocalDateTime.now().toString());

            messagingTemplate.convertAndSend("/topic/staff-chat", staffOnlineNotification);

            log.info("Staff {} đã online và đăng ký chat thành công. Tổng staff online: {}", staffId,
                    onlineStaff.size());

        } catch (Exception e) {
            log.error("Lỗi khi đăng ký staff chat: {}", e.getMessage());
            sendErrorToStaff("REGISTRATION_ERROR", "Lỗi đăng ký chat: " + e.getMessage(),
                    headerAccessor.getSessionId());
        }
    }

    /**
     * Staff phản hồi tin nhắn cụ thể của User
     */
    @MessageMapping("/chat/staff-reply")
    public void handleStaffReplyToUserMessage(@Payload Object payload, SimpMessageHeaderAccessor headerAccessor) {
        try {
            // Sử dụng method có sẵn trong JwtUtil để lấy token từ WebSocket headers
            String token = jwtUtil.getTokenFromWebSocketHeaders(headerAccessor);

            // Validate JWT token
            if (token == null || token.trim().isEmpty()) {
                sendErrorToStaff("TOKEN_REQUIRED", "Token xác thực không được để trống", headerAccessor.getSessionId());
                return;
            }

            if (!jwtUtil.validateToken(token)) {
                sendErrorToStaff("INVALID_TOKEN", "Phiên đăng nhập đã hết hạn", headerAccessor.getSessionId());
                return;
            }

            // Parse payload thành ChatMessageRequest (không có token)
            ChatMessageRequest request = parsePayloadToChatRequestWithoutToken(payload);

            // Validate message content only
            if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                sendErrorToStaff("MESSAGE_REQUIRED", "Nội dung tin nhắn không được để trống",
                        headerAccessor.getSessionId());
                return;
            }

            if (request.getMessage().length() > 1000) {
                sendErrorToStaff("MESSAGE_TOO_LONG", "Tin nhắn không được vượt quá 1000 ký tự",
                        headerAccessor.getSessionId());
                return;
            }

            // Lấy thông tin staff từ token
            String username = jwtUtil.getUsernameFromToken(token);
            User staff = userService.findByUsername(username);

            if (staff == null) {
                sendErrorToStaff("STAFF_NOT_FOUND", "Không tìm thấy thông tin nhân viên",
                        headerAccessor.getSessionId());
                return;
            }

            // Kiểm tra phải là staff hoặc admin
            String roleCode = staff.getRole().getCode();
            if (!"ROLE_STAFF".equals(roleCode) && !"ROLE_ADMIN".equals(roleCode)) {
                sendErrorToStaff("ACCESS_DENIED", "Chỉ nhân viên mới có thể phản hồi tin nhắn",
                        headerAccessor.getSessionId());
                return;
            }

            // Kiểm tra recipientUserId (bắt buộc)
            if (request.getRecipientUserId() == null) {
                sendErrorToStaff("RECIPIENT_USER_ID_REQUIRED", "Cần chỉ định người nhận tin nhắn",
                        headerAccessor.getSessionId());
                return;
            }

            // Tìm user nhận tin nhắn
            User recipientUser = userService.findById(request.getRecipientUserId());
            if (recipientUser == null) {
                sendErrorToStaff("RECIPIENT_USER_NOT_FOUND", "Không tìm thấy người dùng nhận tin nhắn",
                        headerAccessor.getSessionId());
                return;
            }

            // Lưu staff vào online map
            String staffId = staff.getId().toString();
            onlineStaff.put(staffId, headerAccessor.getSessionId());

            // Tạo messageId
            String messageId = UUID.randomUUID().toString();
            ChatMessage savedMessage;

            // Kiểm tra có phải reply tin nhắn cụ thể không
            boolean isReply = request.getReplyToMessageId() != null && !request.getReplyToMessageId().trim().isEmpty();

            if (isReply) {
                // Phản hồi tin nhắn cụ thể
                ChatMessage originalMessage = chatService.findMessageById(request.getReplyToMessageId());
                if (originalMessage == null) {
                    sendErrorToStaff("ORIGINAL_MESSAGE_NOT_FOUND", "Không tìm thấy tin nhắn gốc để phản hồi",
                            headerAccessor.getSessionId());
                    return;
                }

                savedMessage = chatService.sendStaffReplyMessage(
                        staff,
                        request.getMessage(),
                        messageId,
                        request.getReplyToMessageId());

                // Gửi tin nhắn reply tới user cụ thể (kèm thông tin tin nhắn gốc)
                sendReplyToSpecificUser(savedMessage, originalMessage);

                // Gửi xác nhận reply tới staff
                sendReplyConfirmationToStaff(savedMessage, staffId);

                log.info("Staff {} đã phản hồi tin nhắn {} thành công", staffId, request.getReplyToMessageId());

            } else {
                // Gửi tin nhắn bình thường
                savedMessage = chatService.sendStaffToUserMessage(
                        staff,
                        recipientUser,
                        request.getMessage(),
                        messageId);

                // Gửi tin nhắn tới user cụ thể
                sendMessageToSpecificUser(savedMessage);

                // Gửi xác nhận tới staff
                sendMessageConfirmationToStaff(savedMessage, staffId);

                log.info("Staff {} đã gửi tin nhắn cho user {} thành công", staffId, recipientUser.getId());
            }

        } catch (Exception e) {
            log.error("Lỗi khi xử lý tin nhắn từ staff: {}", e.getMessage());
            sendErrorToStaff("MESSAGE_ERROR", "Lỗi khi gửi tin nhắn: " + e.getMessage(), headerAccessor.getSessionId());
        }
    }

    // ========== ONLINE/OFFLINE MANAGEMENT ==========

    /**
     * Xử lý khi có connection disconnect
     */
    public void handleDisconnect(String sessionId) {
        try {
            // Tìm và remove user/staff khỏi online maps
            String disconnectedUserId = null;
            String disconnectedStaffId = null;

            // Tìm user disconnect
            for (Map.Entry<String, String> entry : onlineUsers.entrySet()) {
                if (entry.getValue().equals(sessionId)) {
                    disconnectedUserId = entry.getKey();
                    onlineUsers.remove(entry.getKey());
                    break;
                }
            }

            // Tìm staff disconnect
            for (Map.Entry<String, String> entry : onlineStaff.entrySet()) {
                if (entry.getValue().equals(sessionId)) {
                    disconnectedStaffId = entry.getKey();
                    onlineStaff.remove(entry.getKey());
                    break;
                }
            }

            // Thông báo disconnect
            if (disconnectedUserId != null) {
                log.info("User {} đã offline", disconnectedUserId);
            }

            if (disconnectedStaffId != null) {
                // Thông báo tới các staff khác
                Map<String, Object> staffOfflineNotification = new HashMap<>();
                staffOfflineNotification.put("type", "STAFF_OFFLINE");
                staffOfflineNotification.put("staffId", disconnectedStaffId);
                staffOfflineNotification.put("totalOnlineStaff", onlineStaff.size());
                staffOfflineNotification.put("timestamp", LocalDateTime.now().toString());

                messagingTemplate.convertAndSend("/topic/staff-chat", staffOfflineNotification);
                log.info("Staff {} đã offline. Tổng staff online: {}", disconnectedStaffId, onlineStaff.size());
            }

        } catch (Exception e) {
            log.error("Lỗi khi xử lý disconnect: {}", e.getMessage());
        }
    }

    /**
     * Load tin nhắn chưa đọc cho user khi online
     */
    private void loadUnreadMessagesForUser(String userId, User user) {
        try {
            List<ChatMessageResponse> unreadMessages = chatService.getUnreadMessagesForUser(user);

            if (!unreadMessages.isEmpty()) {
                Map<String, Object> unreadData = new HashMap<>();
                unreadData.put("type", "UNREAD_MESSAGES_LOADED");
                unreadData.put("messages", unreadMessages);
                unreadData.put("count", unreadMessages.size());
                unreadData.put("timestamp", LocalDateTime.now().toString());

                messagingTemplate.convertAndSendToUser(userId, "/queue/chat-messages", unreadData);
                log.info("Đã load {} tin nhắn chưa đọc cho user {}", unreadMessages.size(), userId);
            }
        } catch (Exception e) {
            log.error("Lỗi khi load tin nhắn chưa đọc cho user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Load tin nhắn chưa đọc cho staff khi online
     */
    private void loadUnreadMessagesForStaff(String staffId) {
        try {
            // Lấy tất cả tin nhắn chưa đọc từ user gửi cho staff
            Long unreadCount = chatService.countUnreadUserToStaffMessages();

            if (unreadCount > 0) {
                // Gửi thông báo có tin nhắn chưa đọc
                Map<String, Object> unreadNotification = new HashMap<>();
                unreadNotification.put("type", "UNREAD_MESSAGES_NOTIFICATION");
                unreadNotification.put("unreadCount", unreadCount);
                unreadNotification.put("message", "Bạn có " + unreadCount + " tin nhắn chưa đọc từ khách hàng");
                unreadNotification.put("timestamp", LocalDateTime.now().toString());

                messagingTemplate.convertAndSendToUser(staffId, "/queue/staff-chat", unreadNotification);
                log.info("Đã thông báo {} tin nhắn chưa đọc cho staff {}", unreadCount, staffId);
            }
        } catch (Exception e) {
            log.error("Lỗi khi load tin nhắn chưa đọc cho staff {}: {}", staffId, e.getMessage());
        }
    }

    /**
     * Kiểm tra có staff online không
     */
    private boolean hasOnlineStaff() {
        return !onlineStaff.isEmpty();
    }

    /**
     * Gửi tin nhắn tới tất cả staff online
     */
    private void sendMessageToOnlineStaff(ChatMessage message) {
        Map<String, Object> staffMessage = new HashMap<>();
        staffMessage.put("type", "USER_CHAT_REALTIME");
        staffMessage.put("messageId", message.getMessageId());
        staffMessage.put("userId", message.getSender().getId());
        staffMessage.put("userName", message.getSender().getFullName());
        staffMessage.put("userEmail", message.getSender().getEmail());
        staffMessage.put("userPhone", message.getSender().getPhoneNumber());
        staffMessage.put("message", message.getContent());
        staffMessage.put("timestamp", message.getSentAt().toString());
        staffMessage.put("isRealtime", true);

        // Gửi tới topic để tất cả staff online nhận được
        messagingTemplate.convertAndSend("/topic/staff-chat", staffMessage);
    }

    /**
     * Gửi tin nhắn từ staff tới tất cả user online
     */
    private void broadcastStaffMessageToAllUsers(ChatMessage message) {
        Map<String, Object> broadcastMessage = new HashMap<>();
        broadcastMessage.put("type", "STAFF_BROADCAST");
        broadcastMessage.put("messageId", message.getMessageId());
        broadcastMessage.put("staffId", message.getSender().getId());
        broadcastMessage.put("staffName", message.getSender().getFullName());
        broadcastMessage.put("message", message.getContent());
        broadcastMessage.put("timestamp", message.getSentAt().toString());

        // Gửi tới tất cả user đang online
        for (String userId : onlineUsers.keySet()) {
            messagingTemplate.convertAndSendToUser(userId, "/queue/chat-messages", broadcastMessage);
        }

        log.info("Đã gửi tin nhắn broadcast tới tất cả user online");
    }

    // ========== PRIVATE HELPER METHODS ==========

    private ChatMessageRequest parsePayloadToChatRequest(Object payload) throws Exception {
        if (payload instanceof ChatMessageRequest) {
            return (ChatMessageRequest) payload;
        }

        Map<String, Object> payloadMap = parsePayloadToMap(payload);

        // Debug logging để kiểm tra payload
        log.debug("Payload Map keys: {}", payloadMap.keySet());
        log.debug("Token value from payload: '{}'", payloadMap.get("token"));

        String message = getStringFromMap(payloadMap, "message");
        String token = getStringFromMap(payloadMap, "token");

        // Validate token ngay tại đây
        if (token == null || token.trim().isEmpty()) {
            log.error("Token is null or empty in payload. Payload keys: {}", payloadMap.keySet());
            throw new IllegalArgumentException("Token không được để trống trong payload");
        }

        ChatMessageRequest.ChatMessageRequestBuilder builder = ChatMessageRequest.builder()
                .message(message)
                .token(token.trim());

        // Thêm hỗ trợ recipientUserId và replyToMessageId cho staff-reply
        String recipientUserIdStr = getStringFromMap(payloadMap, "recipientUserId");
        if (recipientUserIdStr != null && !recipientUserIdStr.trim().isEmpty()) {
            try {
                builder.recipientUserId(Long.parseLong(recipientUserIdStr.trim()));
            } catch (NumberFormatException e) {
                log.warn("Không thể parse recipientUserId: {}", recipientUserIdStr);
            }
        }

        String replyToMessageId = getStringFromMap(payloadMap, "replyToMessageId");
        if (replyToMessageId != null && !replyToMessageId.trim().isEmpty()) {
            builder.replyToMessageId(replyToMessageId.trim());
        }

        ChatMessageRequest request = builder.build();
        log.debug(
                "Parsed ChatMessageRequest - message: '{}', token length: {}, recipientUserId: {}, replyToMessageId: '{}'",
                request.getMessage(),
                request.getToken() != null ? request.getToken().length() : "null",
                request.getRecipientUserId(),
                request.getReplyToMessageId());

        return request;
    }

    private ChatRegistrationRequest parsePayloadToRegistrationRequest(Object payload) throws Exception {
        if (payload instanceof ChatRegistrationRequest) {
            return (ChatRegistrationRequest) payload;
        }

        Map<String, Object> payloadMap = parsePayloadToMap(payload);
        return ChatRegistrationRequest.builder()
                .token(getStringFromMap(payloadMap, "token"))
                .build();
    }

    private ChatMessageRequest parsePayloadToReplyRequest(Object payload) throws Exception {
        if (payload instanceof ChatMessageRequest) {
            return (ChatMessageRequest) payload;
        }

        Map<String, Object> payloadMap = parsePayloadToMap(payload);
        return ChatMessageRequest.builder()
                .message(getStringFromMap(payloadMap, "message"))
                .token(getStringFromMap(payloadMap, "token"))
                .replyToMessageId(getStringFromMap(payloadMap, "replyToMessageId")) // Lấy thêm trường replyToMessageId
                .build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parsePayloadToMap(Object payload) throws Exception {
        log.debug("=== PARSING PAYLOAD DEBUG ===");
        log.debug("Payload class: {}", payload != null ? payload.getClass().getName() : "null");
        log.debug("Payload toString: {}", payload);

        if (payload == null) {
            throw new IllegalArgumentException("Payload không được để trống");
        }

        // Trường hợp 1: Payload đã là Map
        if (payload instanceof Map) {
            log.debug("Payload là Map, trả về trực tiếp");
            return (Map<String, Object>) payload;
        }

        // Trường hợp 2: Payload là String JSON
        else if (payload instanceof String) {
            String payloadStr = ((String) payload).trim();
            log.debug("Payload là String: '{}'", payloadStr);

            if (payloadStr.isEmpty()) {
                throw new IllegalArgumentException("Payload string không được để trống");
            }

            // Kiểm tra định dạng JSON
            if (payloadStr.startsWith("{") && payloadStr.endsWith("}")) {
                try {
                    Map<String, Object> result = objectMapper.readValue(payloadStr, Map.class);
                    log.debug("Parse JSON string thành công: {}", result);
                    return result;
                } catch (Exception e) {
                    log.error("Lỗi parse JSON string: {}", e.getMessage());
                    throw new IllegalArgumentException("Định dạng JSON không hợp lệ: " + e.getMessage());
                }
            } else {
                throw new IllegalArgumentException(
                        "String payload phải là JSON hợp lệ (bắt đầu với { và kết thúc với }). Nhận được: "
                                + payloadStr);
            }
        }

        // Trường hợp 3: Payload là byte array
        else if (payload instanceof byte[]) {
            byte[] bytes = (byte[]) payload;
            log.debug("Payload là byte array, length: {}", bytes.length);

            if (bytes.length == 0) {
                throw new IllegalArgumentException("Byte array payload rỗng");
            }

            try {
                String jsonString = new String(bytes, java.nio.charset.StandardCharsets.UTF_8).trim();
                log.debug("Converted byte array to string: '{}'", jsonString);
                log.debug("String length: {}", jsonString.length());
                log.debug("String isEmpty: {}", jsonString.isEmpty());
                log.debug("String starts with {{: {}", jsonString.startsWith("{"));
                log.debug("String ends with }}: {}", jsonString.endsWith("}"));

                if (jsonString.isEmpty()) {
                    throw new IllegalArgumentException("Byte array payload chuyển thành string rỗng");
                }

                // Kiểm tra nếu string không phải JSON, có thể là plain text
                if (!jsonString.startsWith("{") || !jsonString.endsWith("}")) {
                    log.debug("Byte array không chứa JSON, thử tạo JSON object với token");
                    // Có thể payload chỉ là token string, thử wrap thành JSON
                    if (jsonString.length() > 10 && !jsonString.contains(" ")) { // Có vẻ như JWT token
                        String wrappedJson = "{\"token\":\"" + jsonString + "\"}";
                        log.debug("Wrapped token thành JSON: {}", wrappedJson);
                        try {
                            Map<String, Object> result = objectMapper.readValue(wrappedJson, Map.class);
                            log.debug("Parse wrapped JSON thành công: {}", result);
                            return result;
                        } catch (Exception e) {
                            log.error("Lỗi parse wrapped JSON: {}", e.getMessage());
                        }
                    }
                    throw new IllegalArgumentException(
                            "Byte array payload không chứa JSON hợp lệ. Nhận được: '" + jsonString + "'");
                }

                Map<String, Object> result = objectMapper.readValue(jsonString, Map.class);
                log.debug("Parse byte array JSON thành công: {}", result);
                return result;

            } catch (Exception e) {
                log.error("Lỗi xử lý byte array payload: {}", e.getMessage());
                // Log byte array content để debug
                log.debug("Byte array content (first 100 bytes): {}",
                        bytes.length > 100 ? new String(bytes, 0, 100, java.nio.charset.StandardCharsets.UTF_8)
                                : new String(bytes, java.nio.charset.StandardCharsets.UTF_8));
                throw new IllegalArgumentException("Lỗi xử lý byte array payload: " + e.getMessage());
            }
        }

        // Trường hợp 4: Thử convert bằng ObjectMapper
        else {
            log.debug("Thử convert payload type {} bằng ObjectMapper", payload.getClass().getSimpleName());
            try {
                String jsonStr = objectMapper.writeValueAsString(payload);
                log.debug("ObjectMapper serialize thành: {}", jsonStr);
                Map<String, Object> result = objectMapper.readValue(jsonStr, Map.class);
                log.debug("Convert bằng ObjectMapper thành công: {}", result);
                return result;
            } catch (Exception e) {
                log.error("Lỗi convert payload: {}", e.getMessage());
                throw new IllegalArgumentException("Định dạng payload không được hỗ trợ. Payload type: "
                        + payload.getClass().getSimpleName() + ". Error: " + e.getMessage()
                        + ". Chỉ hỗ trợ: JSON Object, JSON String, hoặc byte array chứa JSON");
            }
        }
    }

    private String getStringFromMap(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private void saveUserInfoToSession(SimpMessageHeaderAccessor headerAccessor, User user) {
        Map<String, Object> sessionAttrs = headerAccessor.getSessionAttributes();
        if (sessionAttrs != null) {
            sessionAttrs.put("userId", user.getId().toString());
            sessionAttrs.put("userName", user.getFullName());
            sessionAttrs.put("userEmail", user.getEmail());
        }
    }

    private void sendMessageToStaff(ChatMessage message) {
        Map<String, Object> staffMessage = new HashMap<>();
        staffMessage.put("type", "USER_CHAT");
        staffMessage.put("messageId", message.getMessageId());
        staffMessage.put("userId", message.getSender().getId());
        staffMessage.put("userName", message.getSender().getFullName());
        staffMessage.put("userEmail", message.getSender().getEmail());
        staffMessage.put("userPhone", message.getSender().getPhoneNumber());
        staffMessage.put("message", message.getContent());
        staffMessage.put("timestamp", message.getSentAt().toString());

        messagingTemplate.convertAndSend("/topic/staff-chat", staffMessage);
    }

    private void sendMessageToUser(String userId, ChatMessage message) {
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("type", "STAFF_REPLY");
        userMessage.put("messageId", message.getMessageId());
        userMessage.put("staffId", message.getSender().getId());
        userMessage.put("staffName", message.getSender().getFullName());
        userMessage.put("message", message.getContent());
        userMessage.put("timestamp", message.getSentAt().toString());

        messagingTemplate.convertAndSendToUser(userId, "/queue/chat-messages", userMessage);
    }

    // Gửi tin nhắn phản hồi từ staff tới user cụ thể (kèm thông tin tin nhắn gốc)
    private void sendReplyToSpecificUser(ChatMessage message, ChatMessage originalMessage) {
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("type", "STAFF_REPLY");
        userMessage.put("messageId", message.getMessageId());
        userMessage.put("staffId", message.getSender().getId());
        userMessage.put("staffName", message.getSender().getFullName());
        userMessage.put("message", message.getContent());
        userMessage.put("timestamp", message.getSentAt().toString());
        userMessage.put("isReply", true);

        // Thêm thông tin tin nhắn gốc được reply
        if (originalMessage != null) {
            userMessage.put("replyToMessageId", originalMessage.getMessageId());
            userMessage.put("replyToText", originalMessage.getContent());
            userMessage.put("replyToSenderName", originalMessage.getSender().getFullName());
            if (originalMessage.getSentAt() != null) {
                userMessage.put("replyToTimestamp", originalMessage.getSentAt().toString());
            }

            // Thêm replyContext object cho FE dễ xử lý
            Map<String, Object> replyContext = new HashMap<>();
            replyContext.put("originalText", originalMessage.getContent());
            replyContext.put("originalSender", originalMessage.getSender().getFullName());
            if (originalMessage.getSentAt() != null) {
                replyContext.put("originalTimestamp", originalMessage.getSentAt().toString());
            }
            userMessage.put("replyContext", replyContext);
        }

        // Gửi tới user cụ thể dựa trên ID
        messagingTemplate.convertAndSendToUser(message.getReceiver().getId().toString(), "/queue/chat-messages",
                userMessage);
        log.info("Đã gửi tin nhắn phản hồi từ staff {} tới user {} (reply to: {})",
                message.getSender().getId(),
                message.getReceiver().getId(),
                originalMessage != null ? originalMessage.getMessageId() : "null");
    }

    // Gửi xác nhận tới user khi không có staff online
    private void sendConfirmationToUser(String userId, ChatMessage message) {
        Map<String, Object> confirmMessage = new HashMap<>();
        confirmMessage.put("type", "MESSAGE_SENT");
        confirmMessage.put("messageId", message.getMessageId());
        confirmMessage.put("message",
                "Tin nhắn của bạn đã được gửi thành công. Nhân viên sẽ phản hồi sớm nhất có thể.");
        confirmMessage.put("timestamp", message.getSentAt().toString());

        // Gửi thông báo chỉ khi không có staff online
        messagingTemplate.convertAndSendToUser(userId, "/queue/chat-messages", confirmMessage);
        log.info("Đã gửi thông báo xác nhận cho user {} (không có staff online)", userId);
    }

    // Gửi tin nhắn reply từ user tới tất cả staff online (kèm thông tin tin nhắn gốc)
    private void sendUserReplyMessageToOnlineStaff(ChatMessage message, ChatMessage originalMessage) {
        Map<String, Object> staffMessage = new HashMap<>();
        staffMessage.put("type", "USER_CHAT_REALTIME");
        staffMessage.put("messageId", message.getMessageId());
        staffMessage.put("userId", message.getSender().getId());
        staffMessage.put("userName", message.getSender().getFullName());
        staffMessage.put("userEmail", message.getSender().getEmail());
        staffMessage.put("userPhone", message.getSender().getPhoneNumber());
        staffMessage.put("message", message.getContent());
        staffMessage.put("timestamp", message.getSentAt().toString());
        staffMessage.put("isRealtime", true);
        staffMessage.put("isReply", true);

        // Thêm thông tin tin nhắn gốc được reply
        if (originalMessage != null) {
            staffMessage.put("replyToMessageId", originalMessage.getMessageId());
            staffMessage.put("replyToText", originalMessage.getContent());
            staffMessage.put("replyToSenderName", originalMessage.getSender().getFullName());
            if (originalMessage.getSentAt() != null) {
                staffMessage.put("replyToTimestamp", originalMessage.getSentAt().toString());
            }

            // Thêm replyContext object cho FE dễ xử lý
            Map<String, Object> replyContext = new HashMap<>();
            replyContext.put("originalText", originalMessage.getContent());
            replyContext.put("originalSender", originalMessage.getSender().getFullName());
            if (originalMessage.getSentAt() != null) {
                replyContext.put("originalTimestamp", originalMessage.getSentAt().toString());
            }
            staffMessage.put("replyContext", replyContext);
        }

        // Gửi tới topic để tất cả staff online nhận được
        messagingTemplate.convertAndSend("/topic/staff-chat", staffMessage);
        log.info("Đã gửi tin nhắn reply từ user {} tới staff (reply to: {})",
                message.getSender().getId(),
                originalMessage != null ? originalMessage.getMessageId() : "null");
    }

    // Gửi xác nhận reply tới user khi không có staff online
    private void sendReplyConfirmationToUser(String userId, ChatMessage message, ChatMessage originalMessage) {
        Map<String, Object> confirmMessage = new HashMap<>();
        confirmMessage.put("type", "REPLY_MESSAGE_SENT");
        confirmMessage.put("messageId", message.getMessageId());
        confirmMessage.put("message",
                "Tin nhắn phản hồi của bạn đã được gửi thành công. Nhân viên sẽ phản hồi sớm nhất có thể.");
        confirmMessage.put("timestamp", message.getSentAt().toString());
        confirmMessage.put("isReply", true);

        // Thêm thông tin tin nhắn gốc được reply
        if (originalMessage != null) {
            confirmMessage.put("replyToMessageId", originalMessage.getMessageId());
            confirmMessage.put("replyToText", originalMessage.getContent());
        }

        // Gửi thông báo chỉ khi không có staff online
        messagingTemplate.convertAndSendToUser(userId, "/queue/chat-messages", confirmMessage);
        log.info("Đã gửi xác nhận reply cho user {} (không có staff online)", userId);
    }

    private void sendConfirmationToStaff(ChatMessage message, boolean hasOnlineUsers) {
        Map<String, Object> confirmMessage = new HashMap<>();
        confirmMessage.put("type", "STAFF_MESSAGE_SENT");
        confirmMessage.put("messageId", message.getMessageId());
        confirmMessage.put("message", "Tin nhắn đã được gửi broadcast thành công tới tất cả khách hàng online");
        confirmMessage.put("onlineUserCount", onlineUsers.size());
        confirmMessage.put("timestamp", message.getSentAt().toString());

        // Gửi xác nhận tới staff
        messagingTemplate.convertAndSend("/topic/staff-chat", confirmMessage);
        log.info("Đã gửi xác nhận broadcast tới staff");
    }

    private void sendReplyConfirmationToStaff(ChatMessage message, String staffId) {
        Map<String, Object> confirmMessage = new HashMap<>();
        confirmMessage.put("type", "REPLY_MESSAGE_SENT");
        confirmMessage.put("messageId", message.getMessageId());
        confirmMessage.put("message", "Tin nhắn phản hồi của bạn đã được gửi thành công tới user");
        confirmMessage.put("timestamp", message.getSentAt().toString());

        // Gửi xác nhận tới staff
        messagingTemplate.convertAndSend("/topic/staff-chat", confirmMessage);
        log.info("Đã gửi xác nhận phản hồi tới staff {}", staffId);
    }

    private void sendErrorToUser(String errorCode, String errorMessage, String sessionId) {
        Map<String, Object> error = new HashMap<>();
        error.put("type", "ERROR");
        error.put("errorCode", errorCode);
        error.put("message", errorMessage);
        error.put("timestamp", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/user-chat-errors", error);
    }

    private void sendErrorToStaff(String errorCode, String errorMessage, String sessionId) {
        Map<String, Object> error = new HashMap<>();
        error.put("type", "ERROR");
        error.put("errorCode", errorCode);
        error.put("message", errorMessage);
        error.put("timestamp", LocalDateTime.now().toString());

        messagingTemplate.convertAndSend("/topic/staff-chat-errors", error);
    }

    // Gửi tin nhắn bình thường từ staff tới user cụ thể
    private void sendMessageToSpecificUser(ChatMessage message) {
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("type", "STAFF_MESSAGE");
        userMessage.put("messageId", message.getMessageId());
        userMessage.put("staffId", message.getSender().getId());
        userMessage.put("staffName", message.getSender().getFullName());
        userMessage.put("message", message.getContent());
        userMessage.put("timestamp", message.getSentAt().toString());
        userMessage.put("isReply", false);

        // Gửi tới user cụ thể
        messagingTemplate.convertAndSendToUser(message.getReceiver().getId().toString(), "/queue/chat-messages",
                userMessage);
        log.info("Đã gửi tin nhắn từ staff {} tới user {}", message.getSender().getId(), message.getReceiver().getId());
    }

    // Gửi xác nhận tin nhắn bình thường tới staff
    private void sendMessageConfirmationToStaff(ChatMessage message, String staffId) {
        Map<String, Object> confirmMessage = new HashMap<>();
        confirmMessage.put("type", "MESSAGE_SENT");
        confirmMessage.put("messageId", message.getMessageId());
        confirmMessage.put("message", "Tin nhắn của bạn đã được gửi thành công tới user");
        confirmMessage.put("recipientUserId", message.getReceiver().getId());
        confirmMessage.put("recipientUserName", message.getReceiver().getFullName());
        confirmMessage.put("timestamp", message.getSentAt().toString());
        confirmMessage.put("isReply", false);

        // Gửi xác nhận tới staff
        messagingTemplate.convertAndSend("/topic/staff-chat", confirmMessage);
        log.info("Đã gửi xác nhận tin nhắn tới staff {}", staffId);
    }

    // Method mới để parse payload không có token (dành cho staff-reply)
    private ChatMessageRequest parsePayloadToChatRequestWithoutToken(Object payload) throws Exception {
        if (payload instanceof ChatMessageRequest) {
            return (ChatMessageRequest) payload;
        }

        Map<String, Object> payloadMap = parsePayloadToMap(payload);

        // Debug logging để kiểm tra payload (không log token)
        log.debug("Payload Map keys: {}", payloadMap.keySet());

        String message = getStringFromMap(payloadMap, "message");

        ChatMessageRequest.ChatMessageRequestBuilder builder = ChatMessageRequest.builder()
                .message(message);
        // Không set token vì sẽ lấy từ header/session

        // Thêm hỗ trợ recipientUserId và replyToMessageId cho staff-reply
        String recipientUserIdStr = getStringFromMap(payloadMap, "recipientUserId");
        if (recipientUserIdStr != null && !recipientUserIdStr.trim().isEmpty()) {
            try {
                builder.recipientUserId(Long.parseLong(recipientUserIdStr.trim()));
            } catch (NumberFormatException e) {
                log.warn("Không thể parse recipientUserId: {}", recipientUserIdStr);
            }
        }

        String replyToMessageId = getStringFromMap(payloadMap, "replyToMessageId");
        if (replyToMessageId != null && !replyToMessageId.trim().isEmpty()) {
            builder.replyToMessageId(replyToMessageId.trim());
        }

        ChatMessageRequest request = builder.build();
        log.debug(
                "Parsed ChatMessageRequest (without token) - message: '{}', recipientUserId: {}, replyToMessageId: '{}'",
                request.getMessage(),
                request.getRecipientUserId(),
                request.getReplyToMessageId());

        return request;
    }
}
