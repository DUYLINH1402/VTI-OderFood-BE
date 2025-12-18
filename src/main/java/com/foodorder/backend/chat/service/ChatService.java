package com.foodorder.backend.chat.service;

import com.foodorder.backend.chat.dto.ChatMessageRequest;
import com.foodorder.backend.chat.dto.ChatMessageResponse;
import com.foodorder.backend.chat.entity.ChatMessage;
import com.foodorder.backend.chat.entity.Conversation;
import com.foodorder.backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface cho Chat functionality với hỗ trợ conversation và soft delete
 */
public interface ChatService {

    // ========== MESSAGE OPERATIONS ==========

    /**
     * Gửi tin nhắn từ User tới Staff (tự động tạo hoặc lấy conversation)
     */
    ChatMessage sendUserToStaffMessage(User sender, String content, String messageId, String sessionId);

    /**
     * User phản hồi tin nhắn cụ thể của Staff
     */
    ChatMessage sendUserReplyMessage(User userSender, String content, String messageId, String replyToMessageId, String sessionId);

    /**
     * Gửi tin nhắn từ Staff tới User
     */
    ChatMessage sendStaffToUserMessage(User sender, User receiver, String content, String messageId);

    /**
     * Gửi tin nhắn broadcast từ Staff tới tất cả User (chỉ có 1 staff)
     */
    ChatMessage sendStaffBroadcastMessage(User sender, String content, String messageId);

    /**
     * Staff phản hồi tin nhắn cụ thể của User
     */
    ChatMessage sendStaffReplyMessage(User staffSender, String content, String messageId, String replyToMessageId);

    /**
     * Tìm tin nhắn theo messageId để staff có thể phản hồi
     */
    ChatMessage findMessageById(String messageId);

    /**
     * Đánh dấu tin nhắn đã đọc
     */
    void markMessageAsRead(String messageId);

    // ========== CONVERSATION & HISTORY OPERATIONS ==========

    /**
     * Lấy lịch sử chat của user (chỉ những tin nhắn user chưa xóa)
     */
    List<ChatMessageResponse> getChatHistoryForUser(User user);

    /**
     * Lấy lịch sử chat với phân trang cho user
     */
    Page<ChatMessageResponse> getChatHistoryForUserPageable(User user, Pageable pageable);

    /**
     * Lấy lịch sử chat trong conversation cho staff (chỉ những tin nhắn staff chưa xóa)
     */
    List<ChatMessageResponse> getChatHistoryForStaffInConversation(Long conversationId);

    /**
     * Lấy lịch sử chat với phân trang cho staff
     */
    Page<ChatMessageResponse> getChatHistoryForStaffInConversationPageable(Long conversationId, Pageable pageable);

    // ========== SOFT DELETE OPERATIONS ==========

    /**
     * User xóa tin nhắn (soft delete) - user không thấy nữa nhưng staff vẫn thấy
     */
    void deleteMessageByUser(String messageId, User user);

    /**
     * Staff xóa tin nhắn (soft delete) - staff không thấy nữa
     */
    void deleteMessageByStaff(String messageId, User staff);

    /**
     * User khôi phục tin nhắn đã xóa
     */
    void restoreMessageByUser(String messageId, User user);

    /**
     * Staff khôi phục tin nhắn đã xóa
     */
    void restoreMessageByStaff(String messageId, User staff);

    /**
     * Lấy danh sách tin nhắn đã xóa của user (có thể khôi phục)
     */
    List<ChatMessageResponse> getDeletedMessagesByUser(User user);

    /**
     * Lấy danh sách tin nhắn đã xóa của staff trong conversation
     */
    List<ChatMessageResponse> getDeletedMessagesByStaffInConversation(Long conversationId);

    // ========== UNREAD & NOTIFICATION ==========

    /**
     * Đếm tin nhắn chưa đọc của user
     */
    Long countUnreadMessagesForUser(User user);

    /**
     * Đếm tổng số tin nhắn từ user cụ thể
     */
    Long countTotalMessagesFromUser(User user);

    /**
     * Đếm tin nhắn chưa đọc từ user cụ thể mà staff chưa đọc (cho staff interface)
     */
    Long countUnreadMessagesFromUserForStaff(User user);

    /**
     * Đếm tin nhắn chưa đọc từ tất cả user gửi cho staff
     */
    Long countUnreadUserToStaffMessages();

    /**
     * Đếm tin nhắn chưa đọc từ user cụ thể trong conversation
     */
    Long countUnreadMessagesFromUserInConversation(Long conversationId);

    /**
     * Lấy tin nhắn chưa đọc của user
     */
    List<ChatMessageResponse> getUnreadMessagesForUser(User user);

    // ========== STAFF MANAGEMENT ==========

    /**
     * Lấy tất cả tin nhắn từ user gửi cho staff (cho staff xem tổng quan)
     */
    Page<ChatMessageResponse> getAllUserToStaffMessages(Pageable pageable);

    /**
     * Lấy danh sách user đã chat với staff
     */
    List<User> getUsersChatWithStaff();

    /**
     * Tìm tin nhắn theo messageId
     */
    ChatMessage findByMessageId(String messageId);

    // ========== ADMIN & REPORTING ==========

    /**
     * Lấy tin nhắn trong khoảng thời gian (cho báo cáo)
     */
    List<ChatMessageResponse> getMessagesBetweenDates(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Thống kê tin nhắn theo loại trong khoảng thời gian
     */
    List<Object[]> getMessageStatisticsByType(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Thống kê tin nhắn bị xóa trong khoảng thời gian
     */
    Object[] getDeletedMessageStatistics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Dọn dẹp tin nhắn bị xóa hoàn toàn (cả user và staff đều xóa) sau thời gian nhất định
     */
    void cleanupCompletelyDeletedMessages(int daysAfterDeletion);

    // ========== VALIDATION ==========

    /**
     * Validate và xử lý ChatMessageRequest
     */
    void validateChatMessageRequest(ChatMessageRequest request);
}
