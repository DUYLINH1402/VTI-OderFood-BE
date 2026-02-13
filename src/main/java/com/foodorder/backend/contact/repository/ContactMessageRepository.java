package com.foodorder.backend.contact.repository;

import com.foodorder.backend.contact.entity.ContactMessage;
import com.foodorder.backend.contact.entity.ContactStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository để quản lý tin nhắn liên hệ từ khách hàng
 */
@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    /**
     * Đếm số tin nhắn từ một IP trong khoảng thời gian (dùng cho Rate Limiting)
     */
    @Query("SELECT COUNT(c) FROM ContactMessage c WHERE c.ipAddress = :ipAddress AND c.createdAt >= :since")
    long countByIpAddressSince(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);

    /**
     * Đếm số tin nhắn từ một email trong khoảng thời gian (dùng cho Rate Limiting)
     */
    @Query("SELECT COUNT(c) FROM ContactMessage c WHERE c.email = :email AND c.createdAt >= :since")
    long countByEmailSince(@Param("email") String email, @Param("since") LocalDateTime since);

    /**
     * Lấy danh sách tin nhắn theo trạng thái
     */
    Page<ContactMessage> findByStatus(ContactStatus status, Pageable pageable);

    /**
     * Lấy danh sách tin nhắn theo nhiều trạng thái
     */
    Page<ContactMessage> findByStatusIn(List<ContactStatus> statuses, Pageable pageable);

    /**
     * Tìm kiếm tin nhắn theo keyword (tên, email, nội dung)
     */
    @Query("SELECT c FROM ContactMessage c WHERE " +
           "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.message) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.subject) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<ContactMessage> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Đếm số tin nhắn chưa đọc (PENDING)
     */
    @Query("SELECT COUNT(c) FROM ContactMessage c WHERE c.status = 'PENDING'")
    long countPendingMessages();

    /**
     * Đếm số tin nhắn theo trạng thái
     */
    long countByStatus(ContactStatus status);

    /**
     * Lấy danh sách tin nhắn mới nhất
     */
    @Query("SELECT c FROM ContactMessage c ORDER BY c.createdAt DESC")
    List<ContactMessage> findRecentMessages(Pageable pageable);

    /**
     * Lấy tin nhắn trong khoảng thời gian
     */
    @Query("SELECT c FROM ContactMessage c WHERE c.createdAt BETWEEN :startDate AND :endDate ORDER BY c.createdAt DESC")
    Page<ContactMessage> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Thống kê số tin nhắn theo ngày trong khoảng thời gian
     */
    @Query("SELECT DATE(c.createdAt) as date, COUNT(c) as count FROM ContactMessage c " +
           "WHERE c.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(c.createdAt) ORDER BY DATE(c.createdAt)")
    List<Object[]> countMessagesByDate(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}

