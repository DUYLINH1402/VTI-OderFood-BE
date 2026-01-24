package com.foodorder.backend.comment.dto.response;

import com.foodorder.backend.comment.entity.Comment;
import com.foodorder.backend.comment.entity.CommentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO response trả về thông tin bình luận
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response thông tin bình luận")
public class CommentResponse {

    @Schema(description = "ID của bình luận", example = "1")
    private Long id;

    @Schema(description = "Nội dung bình luận", example = "Món này ngon lắm!")
    private String content;

    @Schema(description = "Loại đối tượng", example = "FOOD")
    private String targetType;

    @Schema(description = "ID đối tượng được bình luận", example = "1")
    private Long targetId;

    @Schema(description = "Trạng thái bình luận", example = "ACTIVE")
    private CommentStatus status;

    @Schema(description = "ID của comment cha (null nếu là comment gốc)")
    private Long parentId;

    @Schema(description = "Thời gian tạo bình luận")
    private LocalDateTime createdAt;

    @Schema(description = "Thời gian cập nhật bình luận")
    private LocalDateTime updatedAt;

    @Schema(description = "Thông tin người bình luận")
    private CommentUserInfo user;

    @Schema(description = "Danh sách các reply (chỉ có khi là comment gốc)")
    private List<CommentResponse> replies;

    @Schema(description = "Số lượng reply")
    private Long replyCount;

    /**
     * Chuyển đổi từ Entity sang DTO
     * @param comment Entity comment
     * @param includeReplies Có bao gồm danh sách reply không
     * @return CommentResponse DTO
     */
    public static CommentResponse fromEntity(Comment comment, boolean includeReplies) {
        CommentResponseBuilder builder = CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .targetType(comment.getTargetType().name())
                .targetId(comment.getTargetId())
                .status(comment.getStatus())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .user(CommentUserInfo.fromUser(comment.getUser()));

        // Nếu cần bao gồm replies
        if (includeReplies && comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            List<CommentResponse> replyResponses = comment.getReplies().stream()
                    .filter(reply -> reply.getStatus() == CommentStatus.ACTIVE)
                    .map(reply -> CommentResponse.fromEntity(reply, false))
                    .collect(Collectors.toList());
            builder.replies(replyResponses);
            builder.replyCount((long) replyResponses.size());
        } else {
            builder.replyCount(0L);
        }

        return builder.build();
    }

    /**
     * Chuyển đổi từ Entity sang DTO (không bao gồm replies)
     */
    public static CommentResponse fromEntity(Comment comment) {
        return fromEntity(comment, false);
    }
}

