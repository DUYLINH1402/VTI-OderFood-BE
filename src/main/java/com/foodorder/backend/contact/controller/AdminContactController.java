package com.foodorder.backend.contact.controller;

import com.foodorder.backend.contact.dto.*;
import com.foodorder.backend.contact.entity.ContactStatus;
import com.foodorder.backend.contact.service.ContactService;
import com.foodorder.backend.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller xử lý API quản lý tin nhắn liên hệ dành cho Admin
 * Yêu cầu quyền ADMIN hoặc STAFF
 */
@RestController
@RequestMapping("/api/admin/contacts")
@RequiredArgsConstructor

@Slf4j
@Tag(name = "Admin Contact", description = "API quản lý tin nhắn liên hệ (Admin/Staff)")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class AdminContactController {

    private final ContactService contactService;

    /**
     * Lấy danh sách tất cả tin nhắn liên hệ (phân trang)
     */
    @Operation(summary = "Lấy danh sách tin nhắn liên hệ", description = "Lấy tất cả tin nhắn liên hệ với phân trang")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Thành công"),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    @GetMapping
    public ResponseEntity<Page<ContactResponse>> getAllContacts(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(contactService.getAllContacts(pageable));
    }

    /**
     * Lấy danh sách tin nhắn theo trạng thái
     */
    @Operation(summary = "Lấy tin nhắn theo trạng thái", description = "Lọc tin nhắn theo trạng thái: PENDING, READ, REPLIED, ARCHIVED")
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<ContactResponse>> getContactsByStatus(
            @PathVariable ContactStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(contactService.getContactsByStatus(status, pageable));
    }

    /**
     * Lấy danh sách tin nhắn theo nhiều trạng thái
     */
    @Operation(summary = "Lấy tin nhắn theo nhiều trạng thái", description = "Lọc tin nhắn theo danh sách trạng thái")
    @GetMapping("/statuses")
    public ResponseEntity<Page<ContactResponse>> getContactsByStatuses(
            @RequestParam List<ContactStatus> statuses,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(contactService.getContactsByStatuses(statuses, pageable));
    }

    /**
     * Tìm kiếm tin nhắn theo keyword
     */
    @Operation(summary = "Tìm kiếm tin nhắn", description = "Tìm kiếm tin nhắn theo tên, email, nội dung hoặc chủ đề")
    @GetMapping("/search")
    public ResponseEntity<Page<ContactResponse>> searchContacts(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(contactService.searchContacts(keyword, pageable));
    }

    /**
     * Lấy chi tiết tin nhắn
     */
    @Operation(summary = "Lấy chi tiết tin nhắn", description = "Xem chi tiết một tin nhắn liên hệ theo ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Thành công"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy tin nhắn")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ContactResponse> getContactById(@PathVariable Long id) {
        return ResponseEntity.ok(contactService.getContactById(id));
    }

    /**
     * Cập nhật trạng thái tin nhắn
     */
    @Operation(summary = "Cập nhật trạng thái tin nhắn", description = "Cập nhật trạng thái và ghi chú cho tin nhắn")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy tin nhắn")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<ContactResponse> updateContactStatus(
            @PathVariable Long id,
            @Valid @RequestBody ContactUpdateRequest request) {
        return ResponseEntity.ok(contactService.updateContactStatus(id, request));
    }

    /**
     * Phản hồi tin nhắn liên hệ
     */
    @Operation(summary = "Phản hồi tin nhắn", description = "Phản hồi tin nhắn liên hệ và tùy chọn gửi email cho khách hàng")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Phản hồi thành công"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy tin nhắn")
    })
    @PostMapping("/{id}/reply")
    public ResponseEntity<ContactResponse> replyToContact(
            @PathVariable Long id,
            @Valid @RequestBody ContactReplyRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long adminId = userDetails.getUser().getId();
        return ResponseEntity.ok(contactService.replyToContact(id, request, adminId));
    }

    /**
     * Xóa tin nhắn (chỉ tin đã archived)
     */
    @Operation(summary = "Xóa tin nhắn", description = "Xóa tin nhắn đã lưu trữ (chỉ Admin)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Xóa thành công"),
        @ApiResponse(responseCode = "400", description = "Tin nhắn chưa được lưu trữ"),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy tin nhắn")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteContact(@PathVariable Long id) {
        contactService.deleteContact(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã xóa tin nhắn thành công"
        ));
    }

    /**
     * Đếm số tin nhắn chưa đọc
     */
    @Operation(summary = "Đếm tin nhắn chưa đọc", description = "Lấy số lượng tin nhắn đang chờ xử lý")
    @GetMapping("/pending/count")
    public ResponseEntity<Map<String, Object>> countPendingMessages() {
        long count = contactService.countPendingMessages();
        return ResponseEntity.ok(Map.of("pendingCount", count));
    }

    /**
     * Lấy thống kê tin nhắn liên hệ
     */
    @Operation(summary = "Thống kê tin nhắn", description = "Lấy thống kê tin nhắn theo trạng thái và theo ngày")
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getContactStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(contactService.getContactStatistics(startDate, endDate));
    }

    /**
     * Lấy danh sách tin nhắn mới nhất (cho Dashboard)
     */
    @Operation(summary = "Tin nhắn mới nhất", description = "Lấy danh sách tin nhắn mới nhất cho Dashboard")
    @GetMapping("/recent")
    public ResponseEntity<List<ContactResponse>> getRecentContacts(
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(contactService.getRecentContacts(limit));
    }
}

