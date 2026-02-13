package com.foodorder.backend.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * DTO response thống kê bình luận
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response thống kê bình luận")
public class CommentStatisticsResponse {

    @Schema(description = "Tổng số bình luận trong hệ thống")
    private long totalComments;

    @Schema(description = "Số bình luận đang active")
    private long activeComments;

    @Schema(description = "Số bình luận đang ẩn")
    private long hiddenComments;

    @Schema(description = "Số bình luận đã xóa (soft delete)")
    private long deletedComments;

    @Schema(description = "Số bình luận hôm nay")
    private long commentsToday;

    @Schema(description = "Số bình luận trong 7 ngày gần nhất")
    private long commentsLast7Days;

    @Schema(description = "Số bình luận trong 30 ngày gần nhất")
    private long commentsLast30Days;
}

