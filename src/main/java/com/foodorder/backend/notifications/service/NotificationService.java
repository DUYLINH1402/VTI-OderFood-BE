package com.foodorder.backend.notifications.service;

import com.foodorder.backend.notifications.dto.NotificationCreateDTO;
import com.foodorder.backend.notifications.dto.NotificationResponseDTO;
import com.foodorder.backend.notifications.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Interface Service cho quản lý thông báo
 * Định nghĩa các phương thức nghiệp vụ cho thông báo User và Staff
 */
public interface NotificationService {

    /**
     * Tạo thông báo mới (cho User hoặc Staff)
     */
    NotificationResponseDTO createNotification(NotificationCreateDTO createDTO);

    // ============ USER NOTIFICATION METHODS ============

    /**
     * Lấy tất cả thông báo của user với phân trang
     */
    Page<NotificationResponseDTO> getAllNotificationsByUser(Long userId, Pageable pageable);

    /**
     * Lấy danh sách thông báo chưa đọc của user
     */
    List<NotificationResponseDTO> getUnreadNotificationsByUser(Long userId);

    /**
     * Đánh dấu một thông báo của user đã đọc
     */
    NotificationResponseDTO markAsReadByUser(Long notificationId, Long userId);

    /**
     * Đánh dấu tất cả thông báo của user là đã đọc
     */
    void markAllAsReadByUser(Long userId);

    /**
     * Đếm số lượng thông báo chưa đọc của user
     */
    Long countUnreadNotificationsByUser(Long userId);

    /**
     * Xóa một thông báo của user
     */
    void deleteNotificationByUser(Long notificationId, Long userId);

    /**
     * Xóa tất cả thông báo của user
     */
    void deleteAllNotificationsByUser(Long userId);

    // ============ STAFF NOTIFICATION METHODS ============

    /**
     * Lấy tất cả thông báo của staff với phân trang
     */
    Page<NotificationResponseDTO> getAllNotificationsByStaff(Long staffId, Pageable pageable);

    /**
     * Lấy danh sách thông báo chưa đọc của staff với phân trang
     */
    Page<NotificationResponseDTO> getUnreadNotificationsByStaff(Long staffId, Pageable pageable);

    /**
     * Đánh dấu một thông báo của staff đã đọc
     */
    NotificationResponseDTO markAsReadByStaff(Long notificationId, Long staffId);

    /**
     * Đánh dấu tất cả thông báo của staff là đã đọc
     */
    void markAllAsReadByStaff(Long staffId);

    /**
     * Đếm số lượng thông báo chưa đọc của staff
     */
    Long countUnreadNotificationsByStaff(Long staffId);

    /**
     * Xóa một thông báo của staff
     */
    void deleteNotificationByStaff(Long notificationId, Long staffId);

    /**
     * Xóa tất cả thông báo của staff
     */
    void deleteAllNotificationsByStaff(Long staffId);

    // ============ COMMON METHODS ============

    /**
     * Chuyển đổi Entity sang DTO
     */
    NotificationResponseDTO convertToDTO(Notification notification);
}
