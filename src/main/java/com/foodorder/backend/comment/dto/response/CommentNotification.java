package com.foodorder.backend.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO dùng để gửi thông báo real-time qua WebSocket khi có comment mới
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Thông báo WebSocket khi có comment mới")
public class CommentNotification {

    @Schema(description = "Loại sự kiện", example = "NEW_COMMENT")
    private CommentEventType eventType;

    @Schema(description = "Loại đối tượng được bình luận", example = "FOOD")
    private String targetType;

    @Schema(description = "ID của đối tượng", example = "1")
    private Long targetId;

    @Schema(description = "Thông tin comment")
    private CommentResponse comment;

    @Schema(description = "Tổng số comment sau khi thêm/xóa")
    private Long totalComments;

    @Schema(description = "Thời gian xảy ra sự kiện")
    private LocalDateTime timestamp;

    /**
     * Enum định nghĩa loại sự kiện comment
     */
    public enum CommentEventType {
        NEW_COMMENT,        // Có comment mới
        NEW_REPLY,          // Có reply mới
        COMMENT_UPDATED,    // Comment được cập nhật
        COMMENT_DELETED     // Comment bị xóa
    }

    /**
     * Factory method tạo notification cho comment mới
     */
    public static CommentNotification newComment(CommentResponse comment, Long totalComments) {
        return CommentNotification.builder()
                .eventType(comment.getParentId() != null ? CommentEventType.NEW_REPLY : CommentEventType.NEW_COMMENT)
                .targetType(comment.getTargetType())
                .targetId(comment.getTargetId())
                .comment(comment)
                .totalComments(totalComments)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Factory method tạo notification cho comment được cập nhật
     */
    public static CommentNotification commentUpdated(CommentResponse comment) {
        return CommentNotification.builder()
                .eventType(CommentEventType.COMMENT_UPDATED)
                .targetType(comment.getTargetType())
                .targetId(comment.getTargetId())
                .comment(comment)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * Factory method tạo notification cho comment bị xóa
     */
    public static CommentNotification commentDeleted(String targetType, Long targetId, Long commentId, Long totalComments) {
        return CommentNotification.builder()
                .eventType(CommentEventType.COMMENT_DELETED)
                .targetType(targetType)
                .targetId(targetId)
                .comment(CommentResponse.builder().id(commentId).build())
                .totalComments(totalComments)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

