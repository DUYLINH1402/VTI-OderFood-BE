package com.foodorder.backend.points.dto.response;

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
public class PointsStatisticsResponse {

    // === TỔNG QUAN HỆ THỐNG ===
    private Long totalUsersWithPoints;       // Tổng số user có điểm
    private Long totalPointsInSystem;        // Tổng điểm trong hệ thống
    private Long totalPointsEarned;          // Tổng điểm đã tích lũy
    private Long totalPointsUsed;            // Tổng điểm đã sử dụng
    private Long totalPointsRefunded;        // Tổng điểm đã hoàn lại
    private Long totalPointsExpired;         // Tổng điểm đã hết hạn

    // === TRUNG BÌNH ===
    private Double averagePointsPerUser;     // Điểm trung bình mỗi user
    private Double averagePointsEarnedPerOrder; // Điểm trung bình tích lũy mỗi đơn
    private Double averagePointsUsedPerOrder;   // Điểm trung bình sử dụng mỗi đơn

    // === PHÂN BỔ THEO LOẠI ===
    private Map<String, Long> pointsByType;  // Tổng điểm theo loại (EARN, USE, REFUND, EXPIRE)
    private Map<String, Long> transactionsByType; // Số giao dịch theo loại

    // === TỶ LỆ ===
    private Double usageRate;                // Tỷ lệ sử dụng điểm (%)
    private Double retentionRate;            // Tỷ lệ giữ lại điểm (%)
}

