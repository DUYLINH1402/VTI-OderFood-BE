package com.foodorder.backend.order.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response wrapper cho dữ liệu phân trang")
public class PageResponse<T> {

    @Schema(description = "Danh sách dữ liệu của trang hiện tại")
    private List<T> data;

    @Schema(description = "Số trang hiện tại (bắt đầu từ 0)", example = "0")
    private int page;

    @Schema(description = "Số lượng phần tử mỗi trang", example = "10")
    private int size;

    @Schema(description = "Tổng số phần tử", example = "100")
    private long total;

    @Schema(description = "Tổng số trang", example = "10")
    private int totalPages;

    @Schema(description = "Có trang tiếp theo không", example = "true")
    private boolean hasNext;

    @Schema(description = "Có trang trước đó không", example = "false")
    private boolean hasPrevious;
}
