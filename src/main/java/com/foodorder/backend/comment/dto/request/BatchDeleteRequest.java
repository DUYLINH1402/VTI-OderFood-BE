package com.foodorder.backend.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.List;

/**
 * DTO request để xóa nhiều bình luận cùng lúc
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request xóa hàng loạt bình luận")
public class BatchDeleteRequest {

    @NotEmpty(message = "Danh sách ID bình luận không được trống")
    @Schema(description = "Danh sách ID các bình luận cần xóa", example = "[1, 2, 3]")
    private List<Long> commentIds;
}

