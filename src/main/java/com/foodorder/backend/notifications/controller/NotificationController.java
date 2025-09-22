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
 * Controller xử lý các API thông báo (tương thích ngược)
 * Endpoints: /api/notifications/*
 * Mặc định dành cho User, khuyến khích sử dụng /api/notifications/user/* và /api/notifications/staff/*
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * GET /api/notifications
     * Lấy tất cả thông báo của user hiện tại (có phân trang)
     * @deprecated Sử dụng /api/notifications/user thay thế
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponseDTO>> getAllNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponseDTO> notifications = notificationService
                .getAllNotificationsByUser(userDetails.getId(), pageable);

        return ResponseEntity.ok(notifications);
    }

    /**
     * GET /api/notifications/unread
     * Lấy danh sách thông báo chưa đọc của user hiện tại
     * @deprecated Sử dụng /api/notifications/user/unread thay thế
     */
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponseDTO>> getUnreadNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<NotificationResponseDTO> unreadNotifications = notificationService
                .getUnreadNotificationsByUser(userDetails.getId());

        return ResponseEntity.ok(unreadNotifications);
    }

    /**
     * GET /api/notifications/unread/count
     * Lấy số lượng thông báo chưa đọc
     * @deprecated Sử dụng /api/notifications/user/unread/count thay thế
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("User {} lấy số lượng thông báo chưa đọc (legacy endpoint)", userDetails.getId());

        Long unreadCount = notificationService.countUnreadNotificationsByUser(userDetails.getId());

        return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
    }

    /**
     * PUT /api/notifications/{id}/read
     * Đánh dấu một thông báo đã đọc
     * @deprecated Sử dụng /api/notifications/user/{id}/read thay thế
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        NotificationResponseDTO notification = notificationService
                .markAsReadByUser(id, userDetails.getId());

        return ResponseEntity.ok(notification);
    }

    /**
     * PUT /api/notifications/read-all
     * Đánh dấu tất cả thông báo là đã đọc
     * @deprecated Sử dụng /api/notifications/user/read-all thay thế
     */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("User {} đánh dấu tất cả thông báo đã đọc (legacy endpoint)", userDetails.getId());

        notificationService.markAllAsReadByUser(userDetails.getId());

        return ResponseEntity.ok(Map.of(
                "message", "Đã đánh dấu tất cả thông báo là đã đọc",
                "status", "success"
        ));
    }
}
