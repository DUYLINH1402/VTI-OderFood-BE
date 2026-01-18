package com.foodorder.backend.points.dto.response;

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
public class PointsTrendAnalyticsResponse {

    // === THÔNG TIN THỜI GIAN ===
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    // === THỐNG KÊ TRONG KHOẢNG THỜI GIAN ===
    private Long totalPointsEarned;       // Tổng điểm tích lũy
    private Long totalPointsUsed;         // Tổng điểm sử dụng
    private Long netPointsChange;         // Thay đổi ròng (tích - dùng)
    private Long totalTransactions;       // Tổng số giao dịch điểm
    private Long uniqueUsersEarned;       // Số user tích điểm
    private Long uniqueUsersUsed;         // Số user dùng điểm

    // === XU HƯỚNG THEO NGÀY ===
    private List<DailyPointsData> dailyTrend;

    // === SO SÁNH VỚI KỲ TRƯỚC ===
    private TrendComparison comparison;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyPointsData {
        private String date;
        private Long pointsEarned;
        private Long pointsUsed;
        private Long netChange;
        private Long transactionCount;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TrendComparison {
        private Long previousPeriodEarned;
        private Long previousPeriodUsed;
        private Double earnedChangePercent;   // % thay đổi so với kỳ trước
        private Double usedChangePercent;     // % thay đổi so với kỳ trước
    }
}

