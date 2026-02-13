package com.foodorder.backend.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * DTO response cho thao tác batch (cập nhật/xóa hàng loạt)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response cho thao tác batch")
public class BatchOperationResponse {

    @Schema(description = "Số bình luận đã xử lý thành công")
    private int successCount;

    @Schema(description = "Số bình luận xử lý thất bại")
    private int failCount;

    @Schema(description = "Danh sách ID bình luận xử lý thất bại")
    private List<Long> failedIds;

    @Schema(description = "Thông báo kết quả")
    private String message;
}

