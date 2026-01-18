package com.foodorder.backend.chatbot.repository;

import com.foodorder.backend.chatbot.entity.ChatbotMessage;
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
 * Repository để quản lý tin nhắn ChatBot
 */
@Repository
public interface ChatbotMessageRepository extends JpaRepository<ChatbotMessage, Long> {

    /**
     * Lấy lịch sử chat theo session ID, sắp xếp theo thời gian
     */
    List<ChatbotMessage> findBySessionIdOrderByCreatedAtAsc(String sessionId);

    /**
     * Lấy tin nhắn gần đây nhất của session (để duy trì context)
     */
    @Query("SELECT cm FROM ChatbotMessage cm WHERE cm.sessionId = :sessionId " +
           "ORDER BY cm.createdAt DESC")
    List<ChatbotMessage> findRecentMessagesBySessionId(@Param("sessionId") String sessionId,
                                                       Pageable pageable);

    /**
     * Lấy lịch sử chat của người dùng
     */
    Page<ChatbotMessage> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Đếm số tin nhắn trong một khoảng thời gian
     */
    @Query("SELECT COUNT(cm) FROM ChatbotMessage cm WHERE cm.createdAt BETWEEN :startTime AND :endTime")
    Long countMessagesBetween(@Param("startTime") LocalDateTime startTime,
                             @Param("endTime") LocalDateTime endTime);

    /**
     * Lấy rating trung bình của bot
     */
    @Query("SELECT AVG(cm.userRating) FROM ChatbotMessage cm WHERE cm.messageType = 'BOT' " +
           "AND cm.userRating IS NOT NULL")
    Optional<Double> getAverageBotRating();

    /**
     * Xóa tin nhắn cũ (để dọn dẹp database)
     */
    void deleteByCreatedAtBefore(LocalDateTime cutoffDate);

    /**
     * Tìm session có hoạt động gần đây
     */
    @Query("SELECT DISTINCT cm.sessionId FROM ChatbotMessage cm " +
           "WHERE cm.createdAt > :since")
    List<String> findActiveSessionsSince(@Param("since") LocalDateTime since);
}
