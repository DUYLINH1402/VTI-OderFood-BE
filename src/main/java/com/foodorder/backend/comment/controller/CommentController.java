package com.foodorder.backend.comment.controller;

import com.foodorder.backend.comment.dto.request.CreateCommentRequest;
import com.foodorder.backend.comment.dto.request.UpdateCommentRequest;
import com.foodorder.backend.comment.dto.response.CommentPageResponse;
import com.foodorder.backend.comment.dto.response.CommentResponse;
import com.foodorder.backend.comment.service.CommentService;
import com.foodorder.backend.like.entity.TargetType;
import com.foodorder.backend.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xử lý các API liên quan đến bình luận (dành cho User)
 */
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "Comment API", description = "API quản lý bình luận")
public class CommentController {

    private final CommentService commentService;

    /**
     * Tạo bình luận mới
     * Yêu cầu đăng nhập
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Tạo bình luận mới", description = "Tạo bình luận cho món ăn, bài viết... Có thể là reply nếu có parentId")
    public ResponseEntity<CommentResponse> createComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CreateCommentRequest request
    ) {
        Long userId = userDetails.getUser().getId();
        CommentResponse response = commentService.createComment(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Cập nhật bình luận của mình
     * Yêu cầu đăng nhập
     */
    @PutMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cập nhật bình luận", description = "Chỉ có thể cập nhật bình luận của chính mình")
    public ResponseEntity<CommentResponse> updateComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request
    ) {
        Long userId = userDetails.getUser().getId();
        CommentResponse response = commentService.updateComment(userId, commentId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Xóa bình luận của mình (soft delete)
     * Yêu cầu đăng nhập
     */
    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Xóa bình luận", description = "Xóa bình luận của chính mình (soft delete)")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId
    ) {
        Long userId = userDetails.getUser().getId();
        commentService.deleteComment(userId, commentId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lấy danh sách bình luận của một đối tượng (món ăn, bài viết...)
     * Không yêu cầu đăng nhập
     */
    @GetMapping("/{targetType}/{targetId}")
    @Operation(summary = "Lấy danh sách bình luận", description = "Lấy danh sách bình luận của một đối tượng (FOOD, BLOG...)")
    public ResponseEntity<CommentPageResponse> getCommentsByTarget(
            @PathVariable String targetType,
            @PathVariable Long targetId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        TargetType type = TargetType.valueOf(targetType.toUpperCase());
        Pageable pageable = PageRequest.of(page, size);
        CommentPageResponse response = commentService.getCommentsByTarget(type, targetId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy chi tiết một bình luận
     * Không yêu cầu đăng nhập
     */
    @GetMapping("/detail/{commentId}")
    @Operation(summary = "Lấy chi tiết bình luận", description = "Lấy thông tin chi tiết của một bình luận")
    public ResponseEntity<CommentResponse> getCommentById(
            @PathVariable Long commentId
    ) {
        CommentResponse response = commentService.getCommentById(commentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách reply của một comment
     * Không yêu cầu đăng nhập
     */
    @GetMapping("/{commentId}/replies")
    @Operation(summary = "Lấy danh sách reply", description = "Lấy danh sách reply của một comment")
    public ResponseEntity<CommentPageResponse> getReplies(
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        CommentPageResponse response = commentService.getReplies(commentId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Đếm số bình luận của một đối tượng
     * Không yêu cầu đăng nhập
     */
    @GetMapping("/count/{targetType}/{targetId}")
    @Operation(summary = "Đếm số bình luận", description = "Đếm số bình luận của một đối tượng")
    public ResponseEntity<Long> countComments(
            @PathVariable String targetType,
            @PathVariable Long targetId
    ) {
        TargetType type = TargetType.valueOf(targetType.toUpperCase());
        long count = commentService.countCommentsByTarget(type, targetId);
        return ResponseEntity.ok(count);
    }

    /**
     * Lấy danh sách bình luận của user hiện tại
     * Yêu cầu đăng nhập
     */
    @GetMapping("/my-comments")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Lấy bình luận của tôi", description = "Lấy danh sách bình luận của user hiện tại")
    public ResponseEntity<CommentPageResponse> getMyComments(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long userId = userDetails.getUser().getId();
        Pageable pageable = PageRequest.of(page, size);
        CommentPageResponse response = commentService.getMyComments(userId, pageable);
        return ResponseEntity.ok(response);
    }
}

