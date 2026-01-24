package com.foodorder.backend.points.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.Map;

/**
 * DTO chứa thống kê tổng quan về điểm thưởng
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response chứa thống kê tổng quan về điểm thưởng")
public class PointsStatisticsResponse {

    // === TỔNG QUAN HỆ THỐNG ===
    @Schema(description = "Tổng số user có điểm", example = "1500")
    private Long totalUsersWithPoints;

    @Schema(description = "Tổng điểm trong hệ thống", example = "500000")
    private Long totalPointsInSystem;

    @Schema(description = "Tổng điểm đã tích lũy", example = "800000")
    private Long totalPointsEarned;

    @Schema(description = "Tổng điểm đã sử dụng", example = "300000")
    private Long totalPointsUsed;

    @Schema(description = "Tổng điểm đã hoàn lại", example = "10000")
    private Long totalPointsRefunded;

    @Schema(description = "Tổng điểm đã hết hạn", example = "5000")
    private Long totalPointsExpired;

    // === TRUNG BÌNH ===
    @Schema(description = "Điểm trung bình mỗi user", example = "333.33")
    private Double averagePointsPerUser;

    @Schema(description = "Điểm trung bình tích lũy mỗi đơn", example = "50")
    private Double averagePointsEarnedPerOrder;

    @Schema(description = "Điểm trung bình sử dụng mỗi đơn", example = "100")
    private Double averagePointsUsedPerOrder;

    // === PHÂN BỔ THEO LOẠI ===
    @Schema(description = "Tổng điểm theo loại", example = "{\"EARN\": 800000, \"USE\": 300000, \"REFUND\": 10000}")
    private Map<String, Long> pointsByType;

    @Schema(description = "Số giao dịch theo loại", example = "{\"EARN\": 5000, \"USE\": 2000, \"REFUND\": 100}")
    private Map<String, Long> transactionsByType;

    // === TỶ LỆ ===
    @Schema(description = "Tỷ lệ sử dụng điểm (%)", example = "37.5")
    private Double usageRate;

    @Schema(description = "Tỷ lệ giữ lại điểm (%)", example = "62.5")
    private Double retentionRate;
}

