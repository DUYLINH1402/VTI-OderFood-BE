package com.foodorder.backend.notifications.service.impl;

import com.foodorder.backend.exception.ResourceNotFoundException;
import com.foodorder.backend.notifications.dto.NotificationCreateDTO;
import com.foodorder.backend.notifications.dto.NotificationResponseDTO;
import com.foodorder.backend.notifications.entity.Notification;
import com.foodorder.backend.notifications.repository.NotificationRepository;
import com.foodorder.backend.notifications.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation của NotificationService
 * Xử lý logic nghiệp vụ cho thông báo User và Staff
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public NotificationResponseDTO createNotification(NotificationCreateDTO createDTO) {
//        log.info("Tạo thông báo mới cho {} ID: {}", createDTO.getRecipientType(), createDTO.getRecipientId());

        Notification notification = Notification.builder()
                .userId(createDTO.getUserId())
                .orderId(createDTO.getOrderId())
                .orderCode(createDTO.getOrderCode())
                .title(createDTO.getTitle())
                .message(createDTO.getMessage())
                .type(createDTO.getType())
                .recipientType(createDTO.getRecipientType())
                .recipientId(createDTO.getRecipientId())
                .isRead(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);
//        log.info("Đã tạo thông báo với ID: {} cho {} ID: {}",
//                savedNotification.getId(), createDTO.getRecipientType(), createDTO.getRecipientId());

        return convertToDTO(savedNotification);
    }

    // ============ USER NOTIFICATION METHODS ============

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponseDTO> getAllNotificationsByUser(Long userId, Pageable pageable) {
//        log.info("Lấy danh sách thông báo của user ID: {} với phân trang", userId);

        Page<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return notifications.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponseDTO> getUnreadNotificationsByUser(Long userId) {
//        log.info("Lấy danh sách thông báo chưa đọc của user ID: {}", userId);

        List<Notification> unreadNotifications = notificationRepository.findUnreadByUserId(userId);
        return unreadNotifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public NotificationResponseDTO markAsReadByUser(Long notificationId, Long userId) {
//        log.info("Đánh dấu thông báo ID: {} đã đọc cho user ID: {}", notificationId, userId);

        Notification notification = notificationRepository.findByIdAndRecipient(
                notificationId, Notification.RecipientType.USER, userId);

        if (notification == null) {
            throw new ResourceNotFoundException("NOTIFICATION_NOT_FOUND");
        }

        if (!notification.getIsRead()) {
            notification.markAsRead();
            notification = notificationRepository.save(notification);
//            log.info("Đã đánh dấu thông báo ID: {} là đã đọc cho user", notificationId);
        }

        return convertToDTO(notification);
    }

    @Override
    public void markAllAsReadByUser(Long userId) {

        int updatedCount = notificationRepository.markAllAsReadByUserId(userId);
//        log.info("Đã đánh dấu {} thông báo là đã đọc cho user ID: {}", updatedCount, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countUnreadNotificationsByUser(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Override
    public void deleteNotificationByUser(Long notificationId, Long userId) {
//        log.info("Xóa thông báo ID: {} của user ID: {}", notificationId, userId);

        Notification notification = notificationRepository.findByIdAndRecipient(
                notificationId, Notification.RecipientType.USER, userId);

        if (notification == null) {
            throw new ResourceNotFoundException("NOTIFICATION_NOT_FOUND");
        }

        notificationRepository.delete(notification);
//        log.info("Đã xóa thành công thông báo ID: {} của user ID: {}", notificationId, userId);
    }

    @Override
    public void deleteAllNotificationsByUser(Long userId) {
//        log.info("Xóa tất cả thông báo của user ID: {}", userId);

        int deletedCount = notificationRepository.deleteAllByUserId(userId);
//        log.info("Đã xóa thành công {} thông báo của user ID: {}", deletedCount, userId);
    }

    // ============ STAFF NOTIFICATION METHODS ============

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponseDTO> getAllNotificationsByStaff(Long staffId, Pageable pageable) {
//        log.info("Lấy danh sách thông báo của staff ID: {} với phân trang", staffId);

        Page<Notification> notifications = notificationRepository.findByStaffIdOrderByCreatedAtDesc(staffId, pageable);
        return notifications.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponseDTO> getUnreadNotificationsByStaff(Long staffId, Pageable pageable) {
//        log.info("Lấy danh sách thông báo chưa đọc của staff ID: {} với phân trang", staffId);

        Page<Notification> unreadNotifications = notificationRepository.findUnreadByStaffIdPageable(staffId, pageable);
        return unreadNotifications.map(this::convertToDTO);
    }

    @Override
    public NotificationResponseDTO markAsReadByStaff(Long notificationId, Long staffId) {
//        log.info("Đánh dấu thông báo ID: {} đã đọc cho staff ID: {}", notificationId, staffId);

        Notification notification = notificationRepository.findByIdAndRecipient(
                notificationId, Notification.RecipientType.STAFF, staffId);

        if (notification == null) {
            throw new ResourceNotFoundException("NOTIFICATION_NOT_FOUND");
        }

        if (!notification.getIsRead()) {
            notification.markAsRead();
            notification = notificationRepository.save(notification);
//            log.info("Đã đánh dấu thông báo ID: {} là đã đọc cho staff", notificationId);
        }

        return convertToDTO(notification);
    }

    @Override
    public void markAllAsReadByStaff(Long staffId) {
//        log.info("Đánh dấu tất cả thông báo đã đọc cho staff ID: {}", staffId);

        int updatedCount = notificationRepository.markAllAsReadByStaffId(staffId);
//        log.info("Đã đánh dấu {} thông báo là đã đọc cho staff ID: {}", updatedCount, staffId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countUnreadNotificationsByStaff(Long staffId) {
        return notificationRepository.countUnreadByStaffId(staffId);
    }

    @Override
    public void deleteNotificationByStaff(Long notificationId, Long staffId) {
//        log.info("Xóa thông báo ID: {} của staff ID: {}", notificationId, staffId);

        Notification notification = notificationRepository.findByIdAndRecipient(
                notificationId, Notification.RecipientType.STAFF, staffId);

        if (notification == null) {
            throw new ResourceNotFoundException("NOTIFICATION_NOT_FOUND");
        }

        notificationRepository.delete(notification);
//        log.info("Đã xóa thành công thông báo ID: {} của staff ID: {}", notificationId, staffId);
    }

    @Override
    public void deleteAllNotificationsByStaff(Long staffId) {
//        log.info("Xóa tất cả thông báo của staff ID: {}", staffId);

        int deletedCount = notificationRepository.deleteAllByStaffId(staffId);
//        log.info("Đã xóa thành công {} thông báo của staff ID: {}", deletedCount, staffId);
    }

    // ============ LEGACY METHODS (Backward Compatibility) ============

    /**
     * @deprecated Sử dụng markAsReadByUser thay thế
     */
    @Deprecated
    public NotificationResponseDTO markAsRead(Long notificationId, Long userId) {
        return markAsReadByUser(notificationId, userId);
    }

    /**
     * @deprecated Sử dụng markAllAsReadByUser thay thế
     */
    @Deprecated
    public void markAllAsRead(Long userId) {
        markAllAsReadByUser(userId);
    }

    /**
     * @deprecated Sử dụng countUnreadNotificationsByUser thay thế
     */
    @Deprecated
    public Long countUnreadNotifications(Long userId) {
        return countUnreadNotificationsByUser(userId);
    }

    // ============ COMMON METHODS ============

    @Override
    public NotificationResponseDTO convertToDTO(Notification notification) {
        return NotificationResponseDTO.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .orderId(notification.getOrderId())
                .orderCode(notification.getOrderCode())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .recipientType(notification.getRecipientType())
                .recipientId(notification.getRecipientId())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}
