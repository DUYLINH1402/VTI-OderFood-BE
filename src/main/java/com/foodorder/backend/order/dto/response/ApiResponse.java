package com.foodorder.backend.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response wrapper chuẩn cho API")
public class ApiResponse<T> {

    @Schema(description = "Trạng thái thành công hay thất bại", example = "true")
    private boolean success;

    @Schema(description = "Thông báo kết quả", example = "Thao tác thành công")
    private String message;

    @Schema(description = "Dữ liệu trả về (có thể null)")
    private Object data;

    public static ApiResponse success(String message) {
        return ApiResponse.builder()
                .success(true)
                .message(message)
                .build();
    }

    public static ApiResponse success(String message, Object data) {
        return ApiResponse.builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static ApiResponse error(String message) {
        return ApiResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
}
