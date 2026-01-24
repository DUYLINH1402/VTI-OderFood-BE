package com.foodorder.backend.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response chứa thông tin lỗi khi xử lý đơn hàng")
public class OrderErrorResponse {

    @Schema(description = "Trạng thái thành công hay thất bại", example = "false")
    private boolean success;

    @Schema(description = "Thông báo lỗi chi tiết", example = "Số điểm không đủ để sử dụng")
    private String message;

    @Schema(description = "Mã lỗi chuẩn hóa", example = "INSUFFICIENT_POINTS")
    private String errorCode;

    @Schema(description = "Timestamp của lỗi (Unix milliseconds)", example = "1705744800000")
    private long timestamp;

    // Các error codes phổ biến cho Order
    public static final String INSUFFICIENT_POINTS = "INSUFFICIENT_POINTS";
    public static final String POINTS_EXCEED_ORDER_VALUE = "POINTS_EXCEED_ORDER_VALUE";
    public static final String POINTS_GUEST_ORDER = "POINTS_GUEST_ORDER";
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
    public static final String COUPON_INVALID = "COUPON_INVALID";
    public static final String FOOD_NOT_FOUND = "FOOD_NOT_FOUND";
    public static final String INVALID_REQUEST = "INVALID_REQUEST";
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
}
