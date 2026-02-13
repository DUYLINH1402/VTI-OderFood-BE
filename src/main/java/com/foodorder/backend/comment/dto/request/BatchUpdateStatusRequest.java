package com.foodorder.backend.comment.dto.request;

import com.foodorder.backend.comment.entity.CommentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

/**
 * DTO request để cập nhật trạng thái nhiều bình luận cùng lúc
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request cập nhật trạng thái nhiều bình luận")
public class BatchUpdateStatusRequest {

    @NotEmpty(message = "Danh sách ID bình luận không được trống")
    @Schema(description = "Danh sách ID các bình luận cần cập nhật", example = "[1, 2, 3]")
    private List<Long> commentIds;

    @NotNull(message = "Trạng thái mới không được null")
    @Schema(description = "Trạng thái mới (ACTIVE, HIDDEN, DELETED)", example = "HIDDEN")
    private CommentStatus status;
}

