package com.foodorder.backend.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO request để tạo bình luận mới
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request tạo bình luận mới")
public class CreateCommentRequest {

    @NotBlank(message = "Nội dung bình luận không được để trống")
    @Size(min = 1, max = 2000, message = "Nội dung bình luận từ 1-2000 ký tự")
    @Schema(description = "Nội dung bình luận", example = "Món này ngon lắm!")
    private String content;

    @NotNull(message = "Loại đối tượng là bắt buộc")
    @Schema(description = "Loại đối tượng (FOOD, BLOG...)", example = "FOOD", allowableValues = {"FOOD", "BLOG", "MOVIE"})
    private String targetType;

    @NotNull(message = "ID đối tượng là bắt buộc")
    @Schema(description = "ID của đối tượng được bình luận", example = "1")
    private Long targetId;

    @Schema(description = "ID của comment cha (nếu là reply)", example = "null")
    private Long parentId;
}

