package com.foodorder.backend.chat.service;

import com.foodorder.backend.chat.entity.Conversation;
import com.foodorder.backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service interface cho Conversation - quản lý cuộc trò chuyện duy nhất giữa User và Staff
 */
public interface ConversationService {

    /**
     * Lấy hoặc tạo conversation cho user (mỗi user chỉ có 1 conversation duy nhất)
     */
    Conversation getOrCreateConversationForUser(User user);

    /**
     * Lấy conversation của user (nếu có) - throw exception nếu không tìm thấy
     */
    Conversation getConversationByUser(User user);

    /**
     * Tìm conversation của user - trả về Optional (không throw exception)
     * Sử dụng cho các trường hợp user mới chưa có conversation
     */
    Optional<Conversation> findConversationByUser(User user);

    /**
     * Lấy conversation theo ID
     */
    Conversation getConversationById(Long conversationId);

    /**
     * Cập nhật thời gian tin nhắn cuối cùng
     */
    void updateLastMessageTime(Conversation conversation);

    /**
     * Thêm ghi chú từ staff
     */
    void addStaffNotes(Long conversationId, String notes, User staff);

    /**
     * Lấy tất cả conversation đang hoạt động (cho staff)
     */
    Page<Conversation> getActiveConversations(Pageable pageable);

    /**
     * Lấy tất cả conversation (bao gồm không hoạt động) cho staff
     */
    Page<Conversation> getAllConversations(Pageable pageable);

    /**
     * Tìm kiếm conversation theo thông tin user
     */
    Page<Conversation> searchConversations(String searchTerm, Pageable pageable);

    /**
     * Lấy conversation có tin nhắn mới nhất
     */
    List<Conversation> getRecentConversations(int limit);

    /**
     * Đếm số conversation đang hoạt động
     */
    Long countActiveConversations();

    /**
     * Đếm số conversation có tin nhắn mới từ thời điểm
     */
    Long countConversationsWithNewMessagesSince(LocalDateTime since);

    /**
     * Vô hiệu hóa conversation
     */
    void deactivateConversation(Long conversationId, User staff);

    /**
     * Kích hoạt lại conversation
     */
    void activateConversation(Long conversationId, User staff);

    /**
     * Lấy conversation có ghi chú từ staff
     */
    Page<Conversation> getConversationsWithStaffNotes(Pageable pageable);

    /**
     * Archive các conversation không hoạt động lâu
     */
    void archiveInactiveConversations(int daysInactive);
}
