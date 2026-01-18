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
 * Controller xử lý các API thông báo cho User (khách hàng)
 * Endpoints: /api/notifications/user/*
 */
@RestController
@RequestMapping("/api/notifications/user")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Notifications", description = "API thông báo dành cho khách hàng")
public class UserNotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Tất cả thông báo", description = "Lấy tất cả thông báo của user hiện tại (có phân trang).")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping
    public ResponseEntity<Page<NotificationResponseDTO>> getAllNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "20") int size) {

//        log.info("User {} lấy danh sách thông báo, page: {}, size: {}",
//                userDetails.getId(), page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<NotificationResponseDTO> notifications = notificationService
                .getAllNotificationsByUser(userDetails.getId(), pageable);

        return ResponseEntity.ok(notifications);
    }

    @Operation(summary = "Thông báo chưa đọc", description = "Lấy danh sách thông báo chưa đọc.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponseDTO>> getUnreadNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

//        log.info("User {} lấy danh sách thông báo chưa đọc", userDetails.getId());

        List<NotificationResponseDTO> unreadNotifications = notificationService
                .getUnreadNotificationsByUser(userDetails.getId());

        return ResponseEntity.ok(unreadNotifications);
    }

    @Operation(summary = "Đếm thông báo chưa đọc", description = "Lấy số lượng thông báo chưa đọc.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

//        log.info("User {} lấy số lượng thông báo chưa đọc", userDetails.getId());

        Long unreadCount = notificationService.countUnreadNotificationsByUser(userDetails.getId());

        return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
    }

    @Operation(summary = "Đánh dấu đã đọc", description = "Đánh dấu một thông báo là đã đọc.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy thông báo")
    })
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDTO> markAsRead(
            @Parameter(description = "ID thông báo") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

//        log.info("User {} đánh dấu thông báo {} đã đọc", userDetails.getId(), id);

        NotificationResponseDTO notification = notificationService
                .markAsReadByUser(id, userDetails.getId());

        return ResponseEntity.ok(notification);
    }

    @Operation(summary = "Đánh dấu tất cả đã đọc", description = "Đánh dấu tất cả thông báo là đã đọc.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

//        log.info("User {} đánh dấu tất cả thông báo đã đọc", userDetails.getId());

        notificationService.markAllAsReadByUser(userDetails.getId());

        return ResponseEntity.ok(Map.of(
                "message", "Đã đánh dấu tất cả thông báo là đã đọc",
                "status", "success"
        ));
    }

    @Operation(summary = "Xóa thông báo", description = "Xóa một thông báo của user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy thông báo")
    })
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
