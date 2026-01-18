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
@Tag(name = "Staff Notifications", description = "API thông báo dành cho Staff/Admin")
public class StaffNotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Tất cả thông báo", description = "Lấy tất cả thông báo của staff hiện tại (có phân trang và sắp xếp).")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping
    public ResponseEntity<Page<NotificationResponseDTO>> getAllNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp") @RequestParam(defaultValue = "desc") String sortDir) {

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

    @Operation(summary = "Thông báo chưa đọc", description = "Lấy danh sách thông báo chưa đọc của staff (có phân trang).")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/unread")
    public ResponseEntity<Page<NotificationResponseDTO>> getUnreadNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "Số trang") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số lượng mỗi trang") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Trường sắp xếp") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Hướng sắp xếp") @RequestParam(defaultValue = "desc") String sortDir) {

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

    @Operation(summary = "Đếm thông báo chưa đọc", description = "Lấy số lượng thông báo chưa đọc.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {

//        log.info("Staff {} lấy số lượng thông báo chưa đọc", userDetails.getId());

        Long unreadCount = notificationService.countUnreadNotificationsByStaff(userDetails.getId());

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