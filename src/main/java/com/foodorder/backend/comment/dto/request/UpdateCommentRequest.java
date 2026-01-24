package com.foodorder.backend.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO request để cập nhật bình luận
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request cập nhật bình luận")
public class UpdateCommentRequest {

    @NotBlank(message = "Nội dung bình luận không được để trống")
    @Size(min = 1, max = 2000, message = "Nội dung bình luận từ 1-2000 ký tự")
    @Schema(description = "Nội dung bình luận mới", example = "Món này ngon lắm, sẽ quay lại!")
    private String content;
}

