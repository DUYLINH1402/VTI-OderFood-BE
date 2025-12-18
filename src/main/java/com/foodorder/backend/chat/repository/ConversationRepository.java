package com.foodorder.backend.chat.repository;

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
 * Repository cho Conversation - quản lý cuộc trò chuyện duy nhất giữa User và Staff
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * Tìm conversation của user (mỗi user chỉ có 1 conversation duy nhất)
     */
    Optional<Conversation> findByUser(User user);

    /**
     * Tìm conversation của user theo user ID
     */
    @Query("SELECT c FROM UserStaffConversation c WHERE c.user.id = :userId")
    Optional<Conversation> findByUserId(@Param("userId") Long userId);

    /**
     * Kiểm tra user đã có conversation chưa
     */
    boolean existsByUser(User user);

    /**
     * Lấy tất cả conversation đang hoạt động, sắp xếp theo tin nhắn cuối cùng
     */
    @Query("SELECT c FROM UserStaffConversation c WHERE c.isActive = true ORDER BY c.lastMessageAt DESC NULLS LAST, c.updatedAt DESC")
    Page<Conversation> findActiveConversationsOrderByLastMessage(Pageable pageable);

    /**
     * Lấy tất cả conversation (bao gồm cả không hoạt động) cho Staff xem
     */
    @Query("SELECT c FROM UserStaffConversation c ORDER BY c.lastMessageAt DESC NULLS LAST, c.updatedAt DESC")
    Page<Conversation> findAllConversationsOrderByLastMessage(Pageable pageable);

    /**
     * Lấy conversation có tin nhắn mới nhất (cho staff theo dõi)
     */
    @Query("SELECT c FROM UserStaffConversation c WHERE c.lastMessageAt IS NOT NULL AND c.isActive = true ORDER BY c.lastMessageAt DESC")
    List<Conversation> findRecentActiveConversations(Pageable pageable);

    /**
     * Đếm số conversation đang hoạt động
     */
    @Query("SELECT COUNT(c) FROM UserStaffConversation c WHERE c.isActive = true")
    Long countActiveConversations();

    /**
     * Đếm số conversation có tin nhắn mới trong khoảng thời gian
     */
    @Query("SELECT COUNT(c) FROM UserStaffConversation c WHERE c.lastMessageAt >= :since AND c.isActive = true")
    Long countConversationsWithNewMessagesSince(@Param("since") LocalDateTime since);

    /**
     * Tìm conversation theo tên user (để staff search)
     */
    @Query("SELECT c FROM UserStaffConversation c WHERE " +
           "LOWER(c.user.fullName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.user.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "c.user.phoneNumber LIKE CONCAT('%', :searchTerm, '%') " +
           "ORDER BY c.lastMessageAt DESC NULLS LAST")
    Page<Conversation> searchConversationsByUser(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Lấy conversation có ghi chú từ staff
     */
    @Query("SELECT c FROM UserStaffConversation c WHERE c.staffNotes IS NOT NULL AND c.staffNotes != '' ORDER BY c.updatedAt DESC")
    Page<Conversation> findConversationsWithStaffNotes(Pageable pageable);

    /**
     * Cập nhật thời gian tin nhắn cuối cùng cho conversation
     */
    @Query("UPDATE UserStaffConversation c SET c.lastMessageAt = :lastMessageAt, c.updatedAt = :updatedAt WHERE c.id = :conversationId")
    void updateLastMessageTime(@Param("conversationId") Long conversationId,
                              @Param("lastMessageAt") LocalDateTime lastMessageAt,
                              @Param("updatedAt") LocalDateTime updatedAt);

    /**
     * Tìm conversation không có hoạt động trong thời gian dài (để archive)
     */
    @Query("SELECT c FROM UserStaffConversation c WHERE " +
           "(c.lastMessageAt IS NULL AND c.createdAt < :cutoffDate) OR " +
           "(c.lastMessageAt IS NOT NULL AND c.lastMessageAt < :cutoffDate)")
    List<Conversation> findInactiveConversationsBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
}
