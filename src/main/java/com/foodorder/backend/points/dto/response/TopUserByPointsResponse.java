package com.foodorder.backend.points.dto.response;

import lombok.*;

/**
 * DTO chứa thông tin xếp hạng user theo điểm thưởng
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopUserByPointsResponse {

    private Integer rank;
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private Integer currentBalance;      // Số dư hiện tại
    private Long totalPointsEarned;      // Tổng điểm đã tích lũy
    private Long totalPointsUsed;        // Tổng điểm đã sử dụng
    private Long totalOrders;            // Tổng số đơn hàng
}

