package com.foodorder.backend.chat.service;

import com.foodorder.backend.chat.dto.ChatMessageRequest;
import com.foodorder.backend.chat.dto.ChatMessageResponse;
import com.foodorder.backend.chat.entity.ChatMessage;
import com.foodorder.backend.chat.entity.Conversation;
import com.foodorder.backend.chat.repository.ChatMessageRepository;
import com.foodorder.backend.exception.BadRequestException;
import com.foodorder.backend.exception.ResourceNotFoundException;
import com.foodorder.backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service implementation cho Chat functionality với hỗ trợ conversation và soft delete
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ConversationService conversationService;

    // ========== MESSAGE OPERATIONS ==========

    @Override
    public ChatMessage sendUserToStaffMessage(User sender, String content, String messageId, String sessionId) {
        log.info("User {} gửi tin nhắn tới staff: {}", sender.getId(), content);

        // Lấy hoặc tạo conversation cho user
        Conversation conversation = conversationService.getOrCreateConversationForUser(sender);

        ChatMessage chatMessage = ChatMessage.fromUserToStaff(conversation, sender, content, messageId, sessionId);
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        // Cập nhật thời gian tin nhắn cuối cùng cho conversation
        conversationService.updateLastMessageTime(conversation);

        return savedMessage;
    }

    @Override
    public ChatMessage sendUserReplyMessage(User userSender, String content, String messageId, String replyToMessageId, String sessionId) {
        log.info("User {} phản hồi tin nhắn {}: {}", userSender.getId(), replyToMessageId, content);

        // Tìm tin nhắn gốc để lấy thông tin conversation
        ChatMessage originalMessage = findMessageById(replyToMessageId);
        if (originalMessage == null) {
            throw new ResourceNotFoundException("ORIGINAL_MESSAGE_NOT_FOUND", "Không tìm thấy tin nhắn gốc để phản hồi");
        }

        // Kiểm tra tin nhắn gốc phải là từ staff gửi cho user
        if (originalMessage.getMessageType() != ChatMessage.MessageType.STAFF_TO_USER) {
            throw new BadRequestException("INVALID_REPLY_TARGET", "Chỉ có thể phản hồi tin nhắn của nhân viên hỗ trợ");
        }

        // Lấy hoặc tạo conversation cho user
        Conversation conversation = conversationService.getOrCreateConversationForUser(userSender);

        // Tạo tin nhắn phản hồi
        ChatMessage replyMessage = ChatMessage.builder()
                .messageId(messageId)
                .conversation(conversation)
                .content(content)
                .sender(userSender)
                .receiver(null) // User gửi cho tất cả staff
                .messageType(ChatMessage.MessageType.USER_TO_STAFF)
                .replyToMessageId(replyToMessageId) // Liên kết với tin nhắn gốc
                .sessionId(sessionId)
                .sentAt(LocalDateTime.now())
                .status(ChatMessage.MessageStatus.SENT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(replyMessage);

        // Cập nhật thời gian tin nhắn cuối cùng cho conversation
        conversationService.updateLastMessageTime(conversation);

        log.info("User {} đã phản hồi tin nhắn {} của staff", userSender.getId(), replyToMessageId);
        return savedMessage;
    }

    @Override
    public ChatMessage sendStaffToUserMessage(User sender, User receiver, String content, String messageId) {
        log.info("Staff {} gửi tin nhắn tới user {}: {}", sender.getId(), receiver.getId(), content);

        // Lấy conversation của receiver
        Conversation conversation = conversationService.getOrCreateConversationForUser(receiver);

        ChatMessage chatMessage = ChatMessage.fromStaffToUser(conversation, sender, receiver, content, messageId);
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);

        // Cập nhật thời gian tin nhắn cuối cùng cho conversation
        conversationService.updateLastMessageTime(conversation);

        return savedMessage;
    }

    @Override
    public ChatMessage sendStaffBroadcastMessage(User sender, String content, String messageId) {
        log.info("Staff {} gửi tin nhắn broadcast: {}", sender.getId(), content);

        // Tạo tin nhắn broadcast (không cần conversation và receiver cụ thể)
        ChatMessage chatMessage = ChatMessage.builder()
                .messageId(messageId)
                .content(content)
                .sender(sender)
                .receiver(null) // Broadcast không có receiver cụ thể
                .messageType(ChatMessage.MessageType.STAFF_TO_USER)
                .sentAt(LocalDateTime.now())
                .status(ChatMessage.MessageStatus.SENT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        log.info("Đã lưu tin nhắn broadcast từ staff {} với messageId: {}", sender.getId(), messageId);

        return savedMessage;
    }

    @Override
    public ChatMessage sendStaffReplyMessage(User staffSender, String content, String messageId, String replyToMessageId) {
        log.info("Staff {} phản hồi tin nhắn {}: {}", staffSender.getId(), replyToMessageId, content);

        // Tìm tin nhắn gốc để lấy thông tin user và conversation
        ChatMessage originalMessage = findMessageById(replyToMessageId);
        if (originalMessage == null) {
            throw new ResourceNotFoundException("ORIGINAL_MESSAGE_NOT_FOUND", "Không tìm thấy tin nhắn gốc để phản hồi");
        }

        // Kiểm tra tin nhắn gốc phải là từ user gửi cho staff
        if (originalMessage.getMessageType() != ChatMessage.MessageType.USER_TO_STAFF) {
            throw new BadRequestException("INVALID_REPLY_TARGET", "Chỉ có thể phản hồi tin nhắn của khách hàng");
        }

        User originalSender = originalMessage.getSender();
        Conversation conversation = originalMessage.getConversation();

        // Tạo tin nhắn phản hồi
        ChatMessage replyMessage = ChatMessage.builder()
                .messageId(messageId)
                .conversation(conversation)
                .content(content)
                .sender(staffSender)
                .receiver(originalSender) // Gửi cho user đã gửi tin nhắn gốc
                .messageType(ChatMessage.MessageType.STAFF_TO_USER)
                .replyToMessageId(replyToMessageId) // Liên kết với tin nhắn gốc
                .sentAt(LocalDateTime.now())
                .status(ChatMessage.MessageStatus.SENT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(replyMessage);

        // Cập nhật thời gian tin nhắn cuối cùng cho conversation
        conversationService.updateLastMessageTime(conversation);

        log.info("Staff {} đã phản hồi tin nhắn {} của user {}", staffSender.getId(), replyToMessageId, originalSender.getId());
        return savedMessage;
    }

    @Override
    public ChatMessage findMessageById(String messageId) {
        // Sử dụng query với fetch eager để tránh lỗi LazyInitializationException
        return chatMessageRepository.findByMessageIdWithDetails(messageId).orElse(null);
    }

    @Override
    public void markMessageAsRead(String messageId) {
        ChatMessage message = chatMessageRepository.findByMessageId(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("CHAT_MESSAGE_NOT_FOUND", "Không tìm thấy tin nhắn"));

        message.markAsRead();
        chatMessageRepository.save(message);
        log.info("Đánh dấu tin nhắn {} đã được đọc", messageId);
    }

    // ========== CONVERSATION & HISTORY OPERATIONS ==========

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getChatHistoryForUser(User user) {
        try {
            Conversation conversation = conversationService.getConversationByUser(user);
            List<ChatMessage> messages = chatMessageRepository.findVisibleMessagesForUserInConversation(conversation);
            return messages.stream()
                    .map(this::convertToResponseWithReplyReference)
                    .collect(Collectors.toList());
        } catch (ResourceNotFoundException e) {
            // User chưa có conversation nào
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getChatHistoryForUserPageable(User user, Pageable pageable) {
        try {
            Conversation conversation = conversationService.getConversationByUser(user);
            Page<ChatMessage> messages = chatMessageRepository.findVisibleMessagesForUserInConversationPageable(conversation, pageable);
            return messages.map(this::convertToResponseWithReplyReference);
        } catch (ResourceNotFoundException e) {
            // User chưa có conversation nào - trả về page rỗng
            return Page.empty(pageable);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getChatHistoryForStaffInConversation(Long conversationId) {
        Conversation conversation = conversationService.getConversationById(conversationId);
        List<ChatMessage> messages = chatMessageRepository.findVisibleMessagesForStaffInConversation(conversation);
        return messages.stream()
                .map(this::convertToResponseWithReplyReference)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getChatHistoryForStaffInConversationPageable(Long conversationId, Pageable pageable) {
        Conversation conversation = conversationService.getConversationById(conversationId);
        Page<ChatMessage> messages = chatMessageRepository.findVisibleMessagesForStaffInConversationPageable(conversation, pageable);
        return messages.map(this::convertToResponseWithReplyReference);
    }

    // ========== HELPER METHOD FOR REPLY REFERENCE ==========

    /**
     * Convert ChatMessage entity sang ChatMessageResponse với đầy đủ thông tin reply-reference
     */
    private ChatMessageResponse convertToResponseWithReplyReference(ChatMessage chatMessage) {
        if (chatMessage.getReplyToMessageId() != null) {
            ChatMessage originalMessage = chatMessageRepository.findByMessageId(chatMessage.getReplyToMessageId()).orElse(null);
            return ChatMessageResponse.fromEntityWithReplyReference(chatMessage, originalMessage);
        }
        return ChatMessageResponse.fromEntity(chatMessage);
    }

    // ========== SOFT DELETE OPERATIONS ==========

    @Override
    public void deleteMessageByUser(String messageId, User user) {
        ChatMessage message = findByMessageId(messageId);

        // Kiểm tra quyền xóa - chỉ sender mới được xóa tin nhắn của mình
        if (!message.getSender().getId().equals(user.getId())) {
            throw new BadRequestException("ACCESS_DENIED", "Bạn chỉ có thể xóa tin nhắn của chính mình");
        }

        message.deleteByUser();
        chatMessageRepository.save(message);
        log.info("User {} đã xóa tin nhắn {}", user.getId(), messageId);
    }

    @Override
    public void deleteMessageByStaff(String messageId, User staff) {
        // Kiểm tra quyền staff
        String roleCode = staff.getRole().getCode();
        if (!"ROLE_STAFF".equals(roleCode) && !"ROLE_ADMIN".equals(roleCode)) {
            throw new BadRequestException("ACCESS_DENIED", "Chỉ staff mới có thể xóa tin nhắn");
        }

        ChatMessage message = findByMessageId(messageId);
        message.deleteByStaff();
        chatMessageRepository.save(message);
        log.info("Staff {} đã xóa tin nhắn {}", staff.getId(), messageId);
    }

    @Override
    public void restoreMessageByUser(String messageId, User user) {
        ChatMessage message = findByMessageId(messageId);

        // Kiểm tra quyền khôi phục
        if (!message.getSender().getId().equals(user.getId())) {
            throw new BadRequestException("ACCESS_DENIED", "Bạn chỉ có thể khôi phục tin nhắn của chính mình");
        }

        message.restoreByUser();
        chatMessageRepository.save(message);
        log.info("User {} đã khôi phục tin nhắn {}", user.getId(), messageId);
    }

    @Override
    public void restoreMessageByStaff(String messageId, User staff) {
        // Kiểm tra quyền staff
        String roleCode = staff.getRole().getCode();
        if (!"ROLE_STAFF".equals(roleCode) && !"ROLE_ADMIN".equals(roleCode)) {
            throw new BadRequestException("ACCESS_DENIED", "Chỉ staff mới có thể khôi phục tin nhắn");
        }

        ChatMessage message = findByMessageId(messageId);
        message.restoreByStaff();
        chatMessageRepository.save(message);
        log.info("Staff {} đã khôi phục tin nhắn {}", staff.getId(), messageId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getDeletedMessagesByUser(User user) {
        try {
            Conversation conversation = conversationService.getConversationByUser(user);
            List<ChatMessage> deletedMessages = chatMessageRepository.findUserDeletedMessagesInConversation(conversation);
            return deletedMessages.stream()
                    .map(ChatMessageResponse::fromEntity)
                    .collect(Collectors.toList());
        } catch (ResourceNotFoundException e) {
            return List.of();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getDeletedMessagesByStaffInConversation(Long conversationId) {
        Conversation conversation = conversationService.getConversationById(conversationId);
        List<ChatMessage> deletedMessages = chatMessageRepository.findStaffDeletedMessagesInConversation(conversation);
        return deletedMessages.stream()
                .map(ChatMessageResponse::fromEntity)
                .collect(Collectors.toList());
    }

    // ========== UNREAD & NOTIFICATION ==========

    @Override
    @Transactional(readOnly = true)
    public Long countUnreadMessagesForUser(User user) {
        try {
            Conversation conversation = conversationService.getConversationByUser(user);
            return chatMessageRepository.countUnreadMessagesForUser(conversation);
        } catch (ResourceNotFoundException e) {
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long countTotalMessagesFromUser(User user) {
        try {
            Conversation conversation = conversationService.getConversationByUser(user);
            return chatMessageRepository.countTotalMessagesFromUser(conversation);
        } catch (ResourceNotFoundException e) {
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long countUnreadMessagesFromUserForStaff(User user) {
        try {
            Conversation conversation = conversationService.getConversationByUser(user);
            return chatMessageRepository.countUnreadMessagesFromUserForStaff(conversation);
        } catch (ResourceNotFoundException e) {
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Long countUnreadUserToStaffMessages() {
        return chatMessageRepository.countUnreadUserToStaffMessages();
    }

    @Override
    @Transactional(readOnly = true)
    public Long countUnreadMessagesFromUserInConversation(Long conversationId) {
        Conversation conversation = conversationService.getConversationById(conversationId);
        return chatMessageRepository.countUnreadMessagesFromUserInConversation(conversation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getUnreadMessagesForUser(User user) {
        try {
            Conversation conversation = conversationService.getConversationByUser(user);
            List<ChatMessage> messages = chatMessageRepository.findVisibleMessagesForUserInConversation(conversation);
            return messages.stream()
                    .filter(msg -> msg.getReadAt() == null && msg.getMessageType() == ChatMessage.MessageType.STAFF_TO_USER)
                    .map(this::convertToResponseWithReplyReference)
                    .collect(Collectors.toList());
        } catch (ResourceNotFoundException e) {
            return List.of();
        }
    }

    // ========== STAFF MANAGEMENT ==========

    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getAllUserToStaffMessages(Pageable pageable) {
        Page<ChatMessage> messages = chatMessageRepository.findAllUserToStaffMessagesVisibleToStaff(pageable);
        return messages.map(this::convertToResponseWithReplyReference);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getUsersChatWithStaff() {
        return chatMessageRepository.findDistinctUsersChatWithStaff();
    }

    @Override
    @Transactional(readOnly = true)
    public ChatMessage findByMessageId(String messageId) {
        return chatMessageRepository.findByMessageId(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("CHAT_MESSAGE_NOT_FOUND", "Không tìm thấy tin nhắn"));
    }

    // ========== ADMIN & REPORTING ==========

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessagesBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
        List<ChatMessage> messages = chatMessageRepository.findMessagesBetweenDates(startDate, endDate);
        return messages.stream()
                .map(ChatMessageResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object[]> getMessageStatisticsByType(LocalDateTime startDate, LocalDateTime endDate) {
        return chatMessageRepository.countMessagesByTypeInPeriod(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public Object[] getDeletedMessageStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        return chatMessageRepository.countDeletedMessagesInPeriod(startDate, endDate);
    }

    @Override
    public void cleanupCompletelyDeletedMessages(int daysAfterDeletion) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysAfterDeletion);
        List<ChatMessage> messagesToDelete = chatMessageRepository.findCompletelyDeletedMessagesBefore(cutoffDate);

        chatMessageRepository.deleteAll(messagesToDelete);
        log.info("Đã xóa {} tin nhắn bị xóa hoàn toàn hơn {} ngày", messagesToDelete.size(), daysAfterDeletion);
    }

    // ========== VALIDATION ==========

    @Override
    public void validateChatMessageRequest(ChatMessageRequest request) {
        if (request == null) {
            throw new BadRequestException("INVALID_REQUEST", "Yêu cầu không hợp lệ");
        }

        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            throw new BadRequestException("MESSAGE_REQUIRED", "Nội dung tin nhắn không được để trống");
        }

        if (request.getMessage().length() > 1000) {
            throw new BadRequestException("MESSAGE_TOO_LONG", "Tin nhắn không được vượt quá 1000 ký tự");
        }

        if (request.getToken() == null || request.getToken().trim().isEmpty()) {
            throw new BadRequestException("TOKEN_REQUIRED", "Token xác thực không được để trống");
        }
    }
}
