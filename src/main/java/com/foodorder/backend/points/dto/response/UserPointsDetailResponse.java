package com.foodorder.backend.points.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Response chứa thông tin chi tiết điểm thưởng của user (dành cho Admin)")
public class UserPointsDetailResponse {

    // === THÔNG TIN USER ===
    @Schema(description = "ID của user", example = "1")
    private Long userId;

    @Schema(description = "Tên đăng nhập", example = "johndoe")
    private String username;

    @Schema(description = "Họ tên đầy đủ", example = "Nguyễn Văn A")
    private String fullName;

    @Schema(description = "Email", example = "user@example.com")
    private String email;

    // === SỐ DƯ HIỆN TẠI ===
    @Schema(description = "Số dư điểm hiện tại", example = "500")
    private Integer currentBalance;

    @Schema(description = "Thời gian cập nhật gần nhất", example = "2025-01-20T10:30:00")
    private LocalDateTime lastUpdated;

    // === THỐNG KÊ TỔNG QUAN ===
    @Schema(description = "Tổng điểm đã tích lũy", example = "2000")
    private Long totalPointsEarned;

    @Schema(description = "Tổng điểm đã sử dụng", example = "1500")
    private Long totalPointsUsed;

    @Schema(description = "Tổng điểm đã hoàn lại", example = "100")
    private Long totalPointsRefunded;

    @Schema(description = "Tổng số giao dịch điểm", example = "75")
    private Long totalTransactions;

    // === LỊCH SỬ GẦN ĐÂY ===
    @Schema(description = "Các giao dịch điểm gần đây")
    private List<PointTransactionDetail> recentTransactions;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Chi tiết một giao dịch điểm")
    public static class PointTransactionDetail {
        @Schema(description = "ID giao dịch", example = "1")
        private Long transactionId;

        @Schema(description = "Loại giao dịch", example = "EARN", allowableValues = {"EARN", "USE", "REFUND", "EXPIRE"})
        private String type;

        @Schema(description = "Số điểm", example = "100")
        private Integer amount;

        @Schema(description = "ID đơn hàng liên quan", example = "50")
        private Long orderId;

        @Schema(description = "Mô tả giao dịch", example = "Tích lũy điểm từ đơn hàng")
        private String description;

        @Schema(description = "Thời gian giao dịch", example = "2025-01-20T10:30:00")
        private LocalDateTime createdAt;
    }
}

