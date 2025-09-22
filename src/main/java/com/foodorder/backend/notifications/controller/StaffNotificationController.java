package com.foodorder.backend.notifications.controller;

import com.foodorder.backend.notifications.dto.NotificationResponseDTO;
import com.foodorder.backend.notifications.service.NotificationService;
import com.foodorder.backend.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller xử lý các API thông báo cho Staff (nhân viên)
 * Endpoints: /api/notifications/staff/*
 * Chỉ Staff và Admin mới có thể truy cập
 */
@RestController
@RequestMapping("/api/notifications/staff")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
public class StaffNotificationController {

    private final NotificationService notificationService;

    /**
     * GET /api/notifications/staff
     * Lấy tất cả thông báo của staff hiện tại (có phân trang và sắp xếp)
     * Mặc định sắp xếp theo createdAt giảm dần (thông báo mới nhất trước)
     */
    @GetMapping
    public ResponseEntity<Page<NotificationResponseDTO>> getAllNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        // Giới hạn size tối đa để tránh tải quá nhiều dữ liệu
        size = Math.min(size, 50);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<NotificationResponseDTO> notifications = notificationService
                .getAllNotificationsByStaff(userDetails.getId(), pageable);

        return ResponseEntity.ok(notifications);
    }

    /**
     * GET /api/notifications/staff/unread
     * Lấy danh sách thông báo chưa đọc của staff hiện tại (có phân trang)
     * Thông báo chưa đọc thường ít nên size mặc định nhỏ hơn
     */
    @GetMapping("/unread")
    public ResponseEntity<Page<NotificationResponseDTO>> getUnreadNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

//        log.info("Staff {} lấy danh sách thông báo chưa đọc, page: {}, size: {}",
//                userDetails.getId(), page, size);

        // Giới hạn size tối đa
        size = Math.min(size, 50);

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<NotificationResponseDTO> unreadNotifications = notificationService
                .getUnreadNotificationsByStaff(userDetails.getId(), pageable);

        return ResponseEntity.ok(unreadNotifications);
    }

    /**
     * GET /api/notifications/staff/unread/count
     * Lấy số lượng thông báo chưa đọc
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

//        log.info("Staff {} lấy số lượng thông báo chưa đọc", userDetails.getId());

        Long unreadCount = notificationService.countUnreadNotificationsByStaff(userDetails.getId());

        return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
    }

    /**
     * PUT /api/notifications/staff/{id}/read
     * Đánh dấu một thông báo đã đọc
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        NotificationResponseDTO notification = notificationService
                .markAsReadByStaff(id, userDetails.getId());

        return ResponseEntity.ok(notification);
    }

    /**
     * PUT /api/notifications/staff/read-all
     * Đánh dấu tất cả thông báo là đã đọc
     */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        notificationService.markAllAsReadByStaff(userDetails.getId());

        return ResponseEntity.ok(Map.of(
                "message", "Đã đánh dấu tất cả thông báo là đã đọc",
                "status", "success"
        ));
    }

    /**
     * DELETE /api/notifications/staff/{id}
     * Xóa một thông báo của staff
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteNotification(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        notificationService.deleteNotificationByStaff(id, userDetails.getId());

        return ResponseEntity.ok(Map.of(
                "message", "Đã xóa thông báo thành công",
                "status", "success"
        ));
    }

    /**
     * DELETE /api/notifications/staff
     * Xóa tất cả thông báo của staff hiện tại
     */
    @DeleteMapping
    public ResponseEntity<Map<String, String>> deleteAllNotifications(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        notificationService.deleteAllNotificationsByStaff(userDetails.getId());

        return ResponseEntity.ok(Map.of(
                "message", "Đã xóa tất cả thông báo thành công",
                "status", "success"
        ));
    }
}