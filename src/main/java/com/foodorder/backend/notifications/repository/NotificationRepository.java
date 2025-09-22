package com.foodorder.backend.notifications.repository;

import com.foodorder.backend.notifications.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho entity Notification
 * Chứa các phương thức truy vấn dữ liệu thông báo cho User và Staff
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // ============ QUERIES FOR USERS ============

    /**
     * Lấy tất cả thông báo của một user với phân trang
     */
    @Query("SELECT n FROM Notification n WHERE n.recipientType = 'USER' AND n.recipientId = :userId ORDER BY n.createdAt DESC")
    Page<Notification> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    /**
     * Lấy danh sách thông báo chưa đọc của một user
     */
    @Query("SELECT n FROM Notification n WHERE n.recipientType = 'USER' AND n.recipientId = :userId AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByUserId(@Param("userId") Long userId);

    /**
     * Đếm số lượng thông báo chưa đọc của một user
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientType = 'USER' AND n.recipientId = :userId AND n.isRead = false")
    Long countUnreadByUserId(@Param("userId") Long userId);

    /**
     * Đánh dấu tất cả thông báo của user là đã đọc
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.recipientType = 'USER' AND n.recipientId = :userId AND n.isRead = false")
    int markAllAsReadByUserId(@Param("userId") Long userId);

    /**
     * Xóa tất cả thông báo của user
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.recipientType = 'USER' AND n.recipientId = :userId")
    int deleteAllByUserId(@Param("userId") Long userId);

    // ============ QUERIES FOR STAFF ============

    /**
     * Lấy tất cả thông báo của staff với phân trang
     */
    @Query("SELECT n FROM Notification n WHERE n.recipientType = 'STAFF' AND n.recipientId = :staffId ORDER BY n.createdAt DESC")
    Page<Notification> findByStaffIdOrderByCreatedAtDesc(@Param("staffId") Long staffId, Pageable pageable);

    /**
     * Lấy danh sách thông báo chưa đọc của staff với phân trang
     */
    @Query("SELECT n FROM Notification n WHERE n.recipientType = 'STAFF' AND n.recipientId = :staffId AND n.isRead = false ORDER BY n.createdAt DESC")
    Page<Notification> findUnreadByStaffIdPageable(@Param("staffId") Long staffId, Pageable pageable);

    /**
     * Lấy danh sách thông báo chưa đọc của staff
     */
    @Query("SELECT n FROM Notification n WHERE n.recipientType = 'STAFF' AND n.recipientId = :staffId AND n.isRead = false ORDER BY n.createdAt DESC")
    List<Notification> findUnreadByStaffId(@Param("staffId") Long staffId);

    /**
     * Đếm số lượng thông báo chưa đọc của staff
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.recipientType = 'STAFF' AND n.recipientId = :staffId AND n.isRead = false")
    Long countUnreadByStaffId(@Param("staffId") Long staffId);

    /**
     * Đánh dấu tất cả thông báo của staff là đã đọc
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.recipientType = 'STAFF' AND n.recipientId = :staffId AND n.isRead = false")
    int markAllAsReadByStaffId(@Param("staffId") Long staffId);

    /**
     * Xóa tất cả thông báo của staff
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.recipientType = 'STAFF' AND n.recipientId = :staffId")
    int deleteAllByStaffId(@Param("staffId") Long staffId);

    // ============ COMMON QUERIES ============

    /**
     * Lấy thông báo theo ID và recipient để đảm bảo bảo mật
     */
    @Query("SELECT n FROM Notification n WHERE n.id = :id AND n.recipientType = :recipientType AND n.recipientId = :recipientId")
    Notification findByIdAndRecipient(@Param("id") Long id,
                                      @Param("recipientType") Notification.RecipientType recipientType,
                                      @Param("recipientId") Long recipientId);

    /**
     * Lấy thông báo theo Order ID (để thông báo cho cả user và staff về cùng một đơn)
     */
    @Query("SELECT n FROM Notification n WHERE n.orderId = :orderId ORDER BY n.createdAt DESC")
    List<Notification> findByOrderId(@Param("orderId") Long orderId);
}
