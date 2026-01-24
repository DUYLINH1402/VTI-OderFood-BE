package com.foodorder.backend.points.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response chứa lịch sử giao dịch điểm thưởng")
public class PointsHistoryDTO {

    @Schema(description = "ID của giao dịch", example = "1")
    private Long id;

    @Schema(description = "Loại giao dịch", example = "EARN", allowableValues = {"EARN", "USE", "REFUND", "EXPIRE"})
    private String type;

    @Schema(description = "Số điểm (dương = tích lũy, âm = sử dụng)", example = "100")
    private Integer amount;

    @Schema(description = "ID đơn hàng liên quan (nếu có)", example = "50")
    private Long orderId;

    @Schema(description = "Mô tả giao dịch", example = "Tích lũy điểm từ đơn hàng #ORD-20250120-001")
    private String description;

    @Schema(description = "Thời gian giao dịch", example = "2025-01-20T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Tổng điểm sau giao dịch", example = "500")
    private Integer totalPointsAfter;
}
