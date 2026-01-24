package com.foodorder.backend.points.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO chứa phân tích xu hướng điểm thưởng theo thời gian
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response chứa phân tích xu hướng điểm thưởng theo thời gian")
public class PointsTrendAnalyticsResponse {

    // === THÔNG TIN THỜI GIAN ===
    @Schema(description = "Thời gian bắt đầu phân tích", example = "2025-01-01T00:00:00")
    private LocalDateTime startDate;

    @Schema(description = "Thời gian kết thúc phân tích", example = "2025-01-31T23:59:59")
    private LocalDateTime endDate;

    // === THỐNG KÊ TRONG KHOẢNG THỜI GIAN ===
    @Schema(description = "Tổng điểm tích lũy trong khoảng thời gian", example = "50000")
    private Long totalPointsEarned;

    @Schema(description = "Tổng điểm sử dụng trong khoảng thời gian", example = "20000")
    private Long totalPointsUsed;

    @Schema(description = "Thay đổi ròng (tích - dùng)", example = "30000")
    private Long netPointsChange;

    @Schema(description = "Tổng số giao dịch điểm", example = "1500")
    private Long totalTransactions;

    @Schema(description = "Số user tích điểm", example = "500")
    private Long uniqueUsersEarned;

    @Schema(description = "Số user dùng điểm", example = "200")
    private Long uniqueUsersUsed;

    // === XU HƯỚNG THEO NGÀY ===
    @Schema(description = "Dữ liệu xu hướng theo ngày")
    private List<DailyPointsData> dailyTrend;

    // === SO SÁNH VỚI KỲ TRƯỚC ===
    @Schema(description = "So sánh với kỳ trước")
    private TrendComparison comparison;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Dữ liệu điểm theo ngày")
    public static class DailyPointsData {
        @Schema(description = "Ngày", example = "2025-01-15")
        private String date;

        @Schema(description = "Điểm tích lũy trong ngày", example = "2000")
        private Long pointsEarned;

        @Schema(description = "Điểm sử dụng trong ngày", example = "800")
        private Long pointsUsed;

        @Schema(description = "Thay đổi ròng trong ngày", example = "1200")
        private Long netChange;

        @Schema(description = "Số giao dịch trong ngày", example = "50")
        private Long transactionCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "So sánh xu hướng với kỳ trước")
    public static class TrendComparison {
        @Schema(description = "Điểm tích lũy kỳ trước", example = "45000")
        private Long previousPeriodEarned;

        @Schema(description = "Điểm sử dụng kỳ trước", example = "18000")
        private Long previousPeriodUsed;

        @Schema(description = "% thay đổi điểm tích lũy so với kỳ trước", example = "11.1")
        private Double earnedChangePercent;

        @Schema(description = "% thay đổi điểm sử dụng so với kỳ trước", example = "11.1")
        private Double usedChangePercent;
    }
}

