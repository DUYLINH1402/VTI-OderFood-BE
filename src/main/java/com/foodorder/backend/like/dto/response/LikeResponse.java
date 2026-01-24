package com.foodorder.backend.like.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response trả về kết quả like/unlike")
public class LikeResponse {

    @Schema(description = "Trạng thái like hiện tại (true = đã like, false = chưa like)", example = "true")
    private boolean liked;

    @Schema(description = "Tổng số lượt like của đối tượng", example = "150")
    private long totalLikes;

    @Schema(description = "Loại đối tượng", example = "FOOD")
    private String targetType;

    @Schema(description = "ID của đối tượng", example = "1")
    private Long targetId;
}

