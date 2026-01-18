package com.foodorder.backend.points.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO chứa thông tin điểm thưởng của một User (dành cho Admin)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPointsDetailResponse {

    // === THÔNG TIN USER ===
    private Long userId;
    private String username;
    private String fullName;
    private String email;

    // === SỐ DƯ HIỆN TẠI ===
    private Integer currentBalance;
    private LocalDateTime lastUpdated;

    // === THỐNG KÊ TỔNG QUAN ===
    private Long totalPointsEarned;       // Tổng điểm đã tích lũy
    private Long totalPointsUsed;         // Tổng điểm đã sử dụng
    private Long totalPointsRefunded;     // Tổng điểm đã hoàn lại
    private Long totalTransactions;       // Tổng số giao dịch điểm

    // === LỊCH SỬ GẦN ĐÂY ===
    private List<PointTransactionDetail> recentTransactions;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PointTransactionDetail {
        private Long transactionId;
        private String type;           // EARN, USE, REFUND, EXPIRE
        private Integer amount;
        private Long orderId;
        private String description;
        private LocalDateTime createdAt;
    }
}

