package com.foodorder.backend.chat.repository;

import com.foodorder.backend.chat.entity.ChatMessage;
import com.foodorder.backend.chat.entity.Conversation;
import com.foodorder.backend.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho Chat Message với hỗ trợ conversation và soft delete
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /**
     * Tìm tin nhắn theo messageId
     */
    Optional<ChatMessage> findByMessageId(String messageId);

    /**
     * Tìm tin nhắn theo messageId với fetch eager sender, receiver và conversation
     * Dùng để tránh lỗi LazyInitializationException
     */
    @Query("SELECT cm FROM UserStaffChatMessage cm " +
           "LEFT JOIN FETCH cm.sender " +
           "LEFT JOIN FETCH cm.receiver " +
           "LEFT JOIN FETCH cm.conversation c " +
           "LEFT JOIN FETCH c.user " +
           "WHERE cm.messageId = :messageId")
    Optional<ChatMessage> findByMessageIdWithDetails(@Param("messageId") String messageId);

    // ========== QUERIES CHO USER (chỉ xem tin nhắn chưa bị user xóa) ==========

    /**
     * Lấy lịch sử chat của user trong conversation (chỉ những tin nhắn user chưa xóa)
     */
    @Query("SELECT cm FROM UserStaffChatMessage cm WHERE cm.conversation = :conversation AND cm.isDeletedByUser = false ORDER BY cm.sentAt ASC")
    List<ChatMessage> findVisibleMessagesForUserInConversation(@Param("conversation") Conversation conversation);

    /**
     * Lấy lịch sử chat của user với phân trang (chỉ những tin nhắn user chưa xóa)
     */
    @Query("SELECT cm FROM UserStaffChatMessage cm WHERE cm.conversation = :conversation AND cm.isDeletedByUser = false ORDER BY cm.sentAt DESC")
    Page<ChatMessage> findVisibleMessagesForUserInConversationPageable(@Param("conversation") Conversation conversation, Pageable pageable);

    /**
     * Đếm tin nhắn chưa đọc của user (chỉ staff gửi cho user và user chưa xóa)
     */
    @Query("SELECT COUNT(cm) FROM UserStaffChatMessage cm WHERE cm.conversation = :conversation AND cm.messageType = 'STAFF_TO_USER' AND cm.readAt IS NULL AND cm.isDeletedByUser = false")
    Long countUnreadMessagesForUser(@Param("conversation") Conversation conversation);

    /**
     * Đếm tổng số tin nhắn từ user cụ thể (chỉ tin nhắn user gửi cho staff và chưa bị xóa)
     */
    @Query("SELECT COUNT(cm) FROM UserStaffChatMessage cm WHERE cm.conversation = :conversation AND cm.messageType = 'USER_TO_STAFF' AND cm.isDeletedByStaff = false")
    Long countTotalMessagesFromUser(@Param("conversation") Conversation conversation);

    /**
     * Đếm tin nhắn chưa đọc từ user cụ thể mà staff chưa đọc (cho staff interface)
     */
    @Query("SELECT COUNT(cm) FROM UserStaffChatMessage cm WHERE cm.conversation = :conversation AND cm.messageType = 'USER_TO_STAFF' AND cm.readAt IS NULL AND cm.isDeletedByStaff = false")
    Long countUnreadMessagesFromUserForStaff(@Param("conversation") Conversation conversation);

    // ========== QUERIES CHO STAFF (chỉ xem tin nhắn chưa bị staff xóa) ==========

    /**
     * Lấy lịch sử chat trong conversation cho staff (chỉ những tin nhắn staff chưa xóa)
     */
    @Query("SELECT cm FROM UserStaffChatMessage cm WHERE cm.conversation = :conversation AND cm.isDeletedByStaff = false ORDER BY cm.sentAt ASC")
    List<ChatMessage> findVisibleMessagesForStaffInConversation(@Param("conversation") Conversation conversation);

    /**
     * Lấy lịch sử chat với phân trang cho staff (chỉ những tin nhắn staff chưa xóa)
     */
    @Query("SELECT cm FROM UserStaffChatMessage cm WHERE cm.conversation = :conversation AND cm.isDeletedByStaff = false ORDER BY cm.sentAt DESC")
    Page<ChatMessage> findVisibleMessagesForStaffInConversationPageable(@Param("conversation") Conversation conversation, Pageable pageable);

    /**
     * Lấy tất cả tin nhắn từ user gửi cho staff (cho staff xem tổng quan)
     */
    @Query("SELECT cm FROM UserStaffChatMessage cm WHERE cm.messageType = 'USER_TO_STAFF' AND cm.isDeletedByStaff = false ORDER BY cm.sentAt DESC")
    Page<ChatMessage> findAllUserToStaffMessagesVisibleToStaff(Pageable pageable);

    /**
     * Đếm tin nhắn chưa đọc từ tất cả user gửi cho staff
     */
    @Query("SELECT COUNT(cm) FROM UserStaffChatMessage cm WHERE cm.messageType = 'USER_TO_STAFF' AND cm.readAt IS NULL AND cm.isDeletedByStaff = false")
    Long countUnreadUserToStaffMessages();

    /**
     * Đếm tin nhắn chưa đọc từ user cụ thể trong conversation
     */
    @Query("SELECT COUNT(cm) FROM UserStaffChatMessage cm WHERE cm.conversation = :conversation AND cm.messageType = 'USER_TO_STAFF' AND cm.readAt IS NULL AND cm.isDeletedByStaff = false")
    Long countUnreadMessagesFromUserInConversation(@Param("conversation") Conversation conversation);

    // ========== QUERIES TỔNG QUÁT (VỚI SOFT DELETE) ==========

    /**
     * Tìm tin nhắn cuối cùng trong conversation (để cập nhật lastMessageAt)
     */
    @Query("SELECT cm FROM UserStaffChatMessage cm WHERE cm.conversation = :conversation ORDER BY cm.sentAt DESC")
    List<ChatMessage> findLastMessageInConversation(@Param("conversation") Conversation conversation, Pageable pageable);

    /**
     * Lấy danh sách user đã chat với staff (từ những conversation có tin nhắn)
     */
    @Query("SELECT DISTINCT c.user FROM UserStaffChatMessage cm JOIN cm.conversation c WHERE cm.messageType = 'USER_TO_STAFF' ORDER BY c.user.fullName")
    List<User> findDistinctUsersChatWithStaff();

    /**
     * Tìm conversation có tin nhắn mới nhất
     */
    @Query("SELECT DISTINCT cm.conversation FROM UserStaffChatMessage cm WHERE cm.sentAt >= :since ORDER BY cm.sentAt DESC")
    List<Conversation> findConversationsWithMessagesSince(@Param("since") LocalDateTime since);

    /**
     * Lấy tin nhắn trong khoảng thời gian (cho báo cáo)
     */
    @Query("SELECT cm FROM UserStaffChatMessage cm WHERE cm.sentAt BETWEEN :startDate AND :endDate ORDER BY cm.sentAt DESC")
    List<ChatMessage> findMessagesBetweenDates(@Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    // ========== SOFT DELETE OPERATIONS ==========

    /**
     * Tìm tin nhắn mà user có thể khôi phục (user đã xóa nhưng staff chưa xóa)
     */
    @Query("SELECT cm FROM UserStaffChatMessage cm WHERE cm.conversation = :conversation AND cm.isDeletedByUser = true AND cm.isDeletedByStaff = false ORDER BY cm.deletedByUserAt DESC")
    List<ChatMessage> findUserDeletedMessagesInConversation(@Param("conversation") Conversation conversation);

    /**
     * Tìm tin nhắn mà staff có thể khôi phục (staff đã xóa nhưng user chưa xóa)
     */
    @Query("SELECT cm FROM UserStaffChatMessage cm WHERE cm.conversation = :conversation AND cm.isDeletedByStaff = true AND cm.isDeletedByUser = false ORDER BY cm.deletedByStaffAt DESC")
    List<ChatMessage> findStaffDeletedMessagesInConversation(@Param("conversation") Conversation conversation);

    /**
     * Tìm tin nhắn bị xóa hoàn toàn (cả user và staff đều xóa) - có thể dùng để cleanup
     */
    @Query("SELECT cm FROM UserStaffChatMessage cm WHERE cm.isDeletedByUser = true AND cm.isDeletedByStaff = true AND cm.deletedByUserAt < :cutoffDate AND cm.deletedByStaffAt < :cutoffDate")
    List<ChatMessage> findCompletelyDeletedMessagesBefore(@Param("cutoffDate") LocalDateTime cutoffDate);

    // ========== ADMIN/MANAGEMENT QUERIES ==========

    /**
     * Thống kê số tin nhắn theo loại trong khoảng thời gian
     */
    @Query("SELECT cm.messageType, COUNT(cm) FROM UserStaffChatMessage cm WHERE cm.sentAt BETWEEN :startDate AND :endDate GROUP BY cm.messageType")
    List<Object[]> countMessagesByTypeInPeriod(@Param("startDate") LocalDateTime startDate,
                                              @Param("endDate") LocalDateTime endDate);

    /**
     * Thống kê số tin nhắn bị xóa
     */
    @Query("SELECT " +
           "COUNT(CASE WHEN cm.isDeletedByUser = true THEN 1 END) as deletedByUser, " +
           "COUNT(CASE WHEN cm.isDeletedByStaff = true THEN 1 END) as deletedByStaff, " +
           "COUNT(CASE WHEN cm.isDeletedByUser = true AND cm.isDeletedByStaff = true THEN 1 END) as completelyDeleted " +
           "FROM UserStaffChatMessage cm WHERE cm.sentAt BETWEEN :startDate AND :endDate")
    Object[] countDeletedMessagesInPeriod(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);
}
