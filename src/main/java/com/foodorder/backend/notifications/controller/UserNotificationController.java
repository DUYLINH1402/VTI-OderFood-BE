package com.foodorder.backend.notifications.controller;

import com.foodorder.backend.notifications.dto.NotificationResponseDTO;
import com.foodorder.backend.notifications.service.NotificationService;
import com.foodorder.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller xử lý các API thông báo cho User (khách hàng)
 * Endpoints: /api/notifications/user/*
 */
@RestController
@RequestMapping("/api/notifications/user")
@RequiredArgsConstructor
@Slf4j
public class UserNotificationController {

    private final NotificationService notificationService;

    /**
     * GET /api/notifications/user
     * Lấy tất cả thông báo của user hiện tại (có phân trang)
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponseDTO>> getAllNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

//        log.info("User {} lấy danh sách thông báo, page: {}, size: {}",
//                userDetails.getId(), page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponseDTO> notifications = notificationService
                .getAllNotificationsByUser(userDetails.getId(), pageable);

        return ResponseEntity.ok(notifications);
    }

    /**
     * GET /api/notifications/user/unread
     * Lấy danh sách thông báo chưa đọc của user hiện tại
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponseDTO>> getUnreadNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

//        log.info("User {} lấy danh sách thông báo chưa đọc", userDetails.getId());

        List<NotificationResponseDTO> unreadNotifications = notificationService
                .getUnreadNotificationsByUser(userDetails.getId());

        return ResponseEntity.ok(unreadNotifications);
    }

    /**
     * GET /api/notifications/user/unread/count
     * Lấy số lượng thông báo chưa đọc
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

//        log.info("User {} lấy số lượng thông báo chưa đọc", userDetails.getId());

        Long unreadCount = notificationService.countUnreadNotificationsByUser(userDetails.getId());

        return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
    }

    /**
     * PUT /api/notifications/user/{id}/read
     * Đánh dấu một thông báo đã đọc
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

//        log.info("User {} đánh dấu thông báo {} đã đọc", userDetails.getId(), id);

        NotificationResponseDTO notification = notificationService
                .markAsReadByUser(id, userDetails.getId());

        return ResponseEntity.ok(notification);
    }

    /**
     * PUT /api/notifications/user/read-all
     * Đánh dấu tất cả thông báo là đã đọc
     */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

//        log.info("User {} đánh dấu tất cả thông báo đã đọc", userDetails.getId());

        notificationService.markAllAsReadByUser(userDetails.getId());

        return ResponseEntity.ok(Map.of(
                "message", "Đã đánh dấu tất cả thông báo là đã đọc",
                "status", "success"
        ));
    }

    /**
     * DELETE /api/notifications/user/{id}
     * Xóa một thông báo của user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

//        log.info("User {} xóa thông báo ID: {}", userDetails.getId(), id);

        notificationService.deleteNotificationByUser(id, userDetails.getId());

        return ResponseEntity.ok(Map.of(
                "message", "Đã xóa thông báo thành công",
                "status", "success"
        ));
    }

    /**
     * DELETE /api/notifications/user/all
     * Xóa tất cả thông báo của user
     */
    @DeleteMapping("/all")
    public ResponseEntity<Map<String, String>> deleteAllNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        notificationService.deleteAllNotificationsByUser(userDetails.getId());

        return ResponseEntity.ok(Map.of(
                "message", "Đã xóa tất cả thông báo thành công",
                "status", "success"
        ));
    }
}
