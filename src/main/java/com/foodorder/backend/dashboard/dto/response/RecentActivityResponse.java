package com.foodorder.backend.dashboard.dto.response;

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
public class RecentActivityResponse {

    // Danh sách hoạt động gần đây
    private List<Activity> activities;

    // Tổng số hoạt động
    private Integer totalActivities;

    /**
     * DTO con chứa thông tin 1 hoạt động
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Activity {
        // Loại hoạt động: ORDER, USER_REGISTER, ORDER_COMPLETED, ORDER_CANCELLED
        private String type;

        // Mô tả hoạt động
        private String description;

        // Thời gian xảy ra
        private LocalDateTime timestamp;

        // ID liên quan (orderId hoặc userId)
        private Long referenceId;

        // Mã đơn hàng (nếu là hoạt động đơn hàng)
        private String orderCode;

        // Tên khách hàng hoặc người dùng liên quan
        private String customerName;

        // Số tiền (nếu là hoạt động đơn hàng)
        private BigDecimal amount;

        // Trạng thái (nếu là đơn hàng)
        private String status;
    }
}
