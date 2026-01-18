package com.foodorder.backend.notifications.controller;

import com.foodorder.backend.notifications.dto.NotificationResponseDTO;
import com.foodorder.backend.notifications.service.NotificationService;
import com.foodorder.backend.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Notifications", description = "API quản lý thông báo - Legacy endpoints")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Lấy tất cả thông báo", description = "Lấy tất cả thông báo của user hiện tại (có phân trang). Deprecated: Sử dụng /api/notifications/user thay thế.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @Deprecated
    @GetMapping
    public ResponseEntity<Page<NotificationResponseDTO>> getAllNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponseDTO> notifications = notificationService
                .getAllNotificationsByUser(userDetails.getId(), pageable);

        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Thông báo chưa đọc", description = "Lấy danh sách thông báo chưa đọc. Deprecated: Sử dụng /api/notifications/user/unread thay thế.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @Deprecated
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponseDTO>> getUnreadNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<NotificationResponseDTO> unreadNotifications = notificationService
                .getUnreadNotificationsByUser(userDetails.getId());

        return ResponseEntity.ok(unreadNotifications);
    }

    @Operation(summary = "Đếm thông báo chưa đọc", description = "Lấy số lượng thông báo chưa đọc. Deprecated: Sử dụng /api/notifications/user/unread/count thay thế.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @Deprecated
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("User {} lấy số lượng thông báo chưa đọc (legacy endpoint)", userDetails.getId());

        Long unreadCount = notificationService.countUnreadNotificationsByUser(userDetails.getId());

        return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
    }

    @Operation(summary = "Đánh dấu đã đọc", description = "Đánh dấu một thông báo là đã đọc. Deprecated: Sử dụng /api/notifications/user/{id}/read thay thế.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy thông báo")
    })
    @Deprecated
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(
            @Parameter(description = "ID của thông báo") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

        NotificationResponseDTO notification = notificationService
                .markAsReadByUser(id, userDetails.getId());

        return ResponseEntity.ok(notification);
    }

    @Operation(summary = "Đánh dấu tất cả đã đọc", description = "Đánh dấu tất cả thông báo là đã đọc. Deprecated: Sử dụng /api/notifications/user/read-all thay thế.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @Deprecated
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("User {} đánh dấu tất cả thông báo đã đọc (legacy endpoint)", userDetails.getId());

        notificationService.markAllAsReadByUser(userDetails.getId());

        return ResponseEntity.ok(Map.of(
                "message", "Đã đánh dấu tất cả thông báo là đã đọc",
                "status", "success"
        ));
    }
}
