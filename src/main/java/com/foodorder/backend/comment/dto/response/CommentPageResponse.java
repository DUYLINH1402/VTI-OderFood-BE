package com.foodorder.backend.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * DTO response trả về danh sách bình luận có phân trang
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response danh sách bình luận có phân trang")
public class CommentPageResponse {

    @Schema(description = "Danh sách bình luận")
    private List<CommentResponse> comments;

    @Schema(description = "Tổng số bình luận", example = "100")
    private long totalComments;

    @Schema(description = "Tổng số trang", example = "10")
    private int totalPages;

    @Schema(description = "Trang hiện tại", example = "0")
    private int currentPage;

    @Schema(description = "Kích thước trang", example = "10")
    private int pageSize;

    @Schema(description = "Có trang tiếp theo không", example = "true")
    private boolean hasNext;

    @Schema(description = "Có trang trước đó không", example = "false")
    private boolean hasPrevious;
}

