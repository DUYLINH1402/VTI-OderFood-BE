package com.foodorder.backend.comment.dto.request;

import com.foodorder.backend.comment.entity.CommentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * DTO request để admin thay đổi trạng thái bình luận
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request thay đổi trạng thái bình luận (dành cho Admin)")
public class UpdateCommentStatusRequest {

    @NotNull(message = "Trạng thái mới là bắt buộc")
    @Schema(description = "Trạng thái mới của bình luận", example = "HIDDEN", allowableValues = {"ACTIVE", "HIDDEN", "DELETED"})
    private CommentStatus status;
}

