package com.foodorder.backend.comment.controller;

import com.foodorder.backend.comment.dto.request.BatchDeleteRequest;
import com.foodorder.backend.comment.dto.request.BatchUpdateStatusRequest;
import com.foodorder.backend.comment.dto.request.UpdateCommentStatusRequest;
import com.foodorder.backend.comment.dto.response.BatchOperationResponse;
import com.foodorder.backend.comment.dto.response.CommentPageResponse;
import com.foodorder.backend.comment.dto.response.CommentResponse;
import com.foodorder.backend.comment.dto.response.CommentStatisticsResponse;
import com.foodorder.backend.comment.entity.CommentStatus;
import com.foodorder.backend.comment.service.CommentService;
import com.foodorder.backend.like.entity.TargetType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các API quản lý bình luận (dành cho Admin)
 */
@RestController
@RequestMapping("/api/admin/comments")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Comment API", description = "API quản lý bình luận dành cho Admin")
public class AdminCommentController {

    private final CommentService commentService;

    /**
     * Lấy tất cả bình luận (phân trang)
     */
    @GetMapping
    @Operation(summary = "Lấy tất cả bình luận", description = "Lấy danh sách tất cả bình luận trong hệ thống")
    public ResponseEntity<CommentPageResponse> getAllComments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        CommentPageResponse response = commentService.getAllComments(pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy bình luận theo trạng thái
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Lấy bình luận theo trạng thái", description = "Lọc bình luận theo trạng thái (ACTIVE, HIDDEN, DELETED)")
    public ResponseEntity<CommentPageResponse> getCommentsByStatus(
            @PathVariable CommentStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        CommentPageResponse response = commentService.getCommentsByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Tìm kiếm bình luận theo nội dung
     */
    @GetMapping("/search")
    @Operation(summary = "Tìm kiếm bình luận", description = "Tìm kiếm bình luận theo nội dung")
    public ResponseEntity<CommentPageResponse> searchComments(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        CommentPageResponse response = commentService.searchComments(keyword, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Thay đổi trạng thái bình luận (ẩn/hiện/xóa)
     */
    @PutMapping("/{commentId}/status")
    @Operation(summary = "Thay đổi trạng thái bình luận", description = "Ẩn, hiện hoặc xóa một bình luận")
    public ResponseEntity<CommentResponse> updateCommentStatus(
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentStatusRequest request
    ) {
        CommentResponse response = commentService.updateCommentStatus(commentId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa vĩnh viễn bình luận (hard delete)
     */
    @DeleteMapping("/{commentId}/hard-delete")
    @Operation(summary = "Xóa vĩnh viễn bình luận", description = "Xóa vĩnh viễn một bình luận khỏi database")
    public ResponseEntity<Void> hardDeleteComment(
            @PathVariable Long commentId
    ) {
        commentService.hardDeleteComment(commentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lấy chi tiết một bình luận
     */
    @GetMapping("/{commentId}")
    @Operation(summary = "Lấy chi tiết bình luận", description = "Lấy thông tin chi tiết của một bình luận (bao gồm cả HIDDEN, DELETED)")
    public ResponseEntity<CommentResponse> getCommentById(
            @PathVariable Long commentId
    ) {
        CommentResponse response = commentService.getCommentById(commentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy tất cả bình luận của một user
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "Lấy bình luận theo user", description = "Lấy danh sách tất cả bình luận của một user cụ thể")
    public ResponseEntity<CommentPageResponse> getCommentsByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        CommentPageResponse response = commentService.getCommentsByUser(userId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy bình luận theo đối tượng (món ăn/bài viết)
     */
    @GetMapping("/target/{targetType}/{targetId}")
    @Operation(summary = "Lấy bình luận theo đối tượng",
               description = "Lấy danh sách bình luận của một đối tượng cụ thể (bao gồm cả HIDDEN, DELETED)")
    public ResponseEntity<CommentPageResponse> getCommentsByTarget(
            @PathVariable String targetType,
            @PathVariable Long targetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        TargetType type = TargetType.valueOf(targetType.toUpperCase());
        Pageable pageable = PageRequest.of(page, size);
        CommentPageResponse response = commentService.getCommentsByTargetForAdmin(type, targetId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Thống kê bình luận
     */
    @GetMapping("/statistics")
    @Operation(summary = "Thống kê bình luận", description = "Lấy thống kê tổng quan về bình luận trong hệ thống")
    public ResponseEntity<CommentStatisticsResponse> getCommentStatistics() {
        CommentStatisticsResponse response = commentService.getCommentStatistics();
        return ResponseEntity.ok(response);
    }

    /**
     * Cập nhật trạng thái nhiều bình luận cùng lúc
     */
    @PutMapping("/batch/status")
    @Operation(summary = "Cập nhật trạng thái hàng loạt", description = "Cập nhật trạng thái cho nhiều bình luận cùng lúc")
    public ResponseEntity<BatchOperationResponse> batchUpdateStatus(
            @Valid @RequestBody BatchUpdateStatusRequest request
    ) {
        BatchOperationResponse response = commentService.batchUpdateStatus(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa vĩnh viễn nhiều bình luận cùng lúc
     */
    @DeleteMapping("/batch/hard-delete")
    @Operation(summary = "Xóa vĩnh viễn hàng loạt", description = "Xóa vĩnh viễn nhiều bình luận khỏi database")
    public ResponseEntity<BatchOperationResponse> batchHardDelete(
            @Valid @RequestBody BatchDeleteRequest request
    ) {
        BatchOperationResponse response = commentService.batchHardDelete(request);
        return ResponseEntity.ok(response);
    }
}

