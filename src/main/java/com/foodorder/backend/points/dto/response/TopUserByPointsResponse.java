package com.foodorder.backend.points.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * DTO chứa thông tin xếp hạng user theo điểm thưởng
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response chứa thông tin xếp hạng user theo điểm thưởng")
public class TopUserByPointsResponse {

    @Schema(description = "Thứ hạng", example = "1")
    private Integer rank;

    @Schema(description = "ID của user", example = "1")
    private Long userId;

    @Schema(description = "Tên đăng nhập", example = "johndoe")
    private String username;

    @Schema(description = "Họ tên đầy đủ", example = "Nguyễn Văn A")
    private String fullName;

    @Schema(description = "Email", example = "user@example.com")
    private String email;

    @Schema(description = "Số dư điểm hiện tại", example = "500")
    private Integer currentBalance;

    @Schema(description = "Tổng điểm đã tích lũy", example = "2000")
    private Long totalPointsEarned;

    @Schema(description = "Tổng điểm đã sử dụng", example = "1500")
    private Long totalPointsUsed;

    @Schema(description = "Tổng số đơn hàng", example = "50")
    private Long totalOrders;
}

