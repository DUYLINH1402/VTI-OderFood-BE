package com.foodorder.backend.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO trả về danh sách hoạt động gần đây
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response chứa danh sách hoạt động gần đây")
public class RecentActivityResponse {

    @Schema(description = "Danh sách hoạt động gần đây")
    private List<Activity> activities;

    @Schema(description = "Tổng số hoạt động", example = "10")
    private Integer totalActivities;

    /**
     * DTO con chứa thông tin 1 hoạt động
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Thông tin một hoạt động")
    public static class Activity {
        @Schema(description = "Loại hoạt động", example = "ORDER", allowableValues = {"ORDER", "USER_REGISTER", "ORDER_COMPLETED", "ORDER_CANCELLED"})
        private String type;

        @Schema(description = "Mô tả hoạt động", example = "Đơn hàng mới #ORD-20250120-001")
        private String description;

        @Schema(description = "Thời gian xảy ra", example = "2025-01-20T10:30:00")
        private LocalDateTime timestamp;

        @Schema(description = "ID liên quan (orderId hoặc userId)", example = "100")
        private Long referenceId;

        @Schema(description = "Mã đơn hàng (nếu là hoạt động đơn hàng)", example = "ORD-20250120-001")
        private String orderCode;

        @Schema(description = "Tên khách hàng hoặc người dùng liên quan", example = "Nguyễn Văn A")
        private String customerName;

        @Schema(description = "Số tiền (nếu là hoạt động đơn hàng)", example = "150000")
        private BigDecimal amount;

        @Schema(description = "Trạng thái (nếu là đơn hàng)", example = "CONFIRMED")
        private String status;
    }
}
