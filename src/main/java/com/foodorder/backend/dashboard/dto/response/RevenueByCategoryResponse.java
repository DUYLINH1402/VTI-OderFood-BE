package com.foodorder.backend.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO cho API cơ cấu doanh thu theo nhóm món
 * Hiển thị 3 nhóm chính và 1 nhóm "Khác" cho các danh mục còn lại
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response chứa cơ cấu doanh thu theo danh mục món ăn")
public class RevenueByCategoryResponse {

    @Schema(description = "Danh sách cơ cấu doanh thu (3 nhóm chính + 1 nhóm Khác)")
    private List<CategoryRevenue> categories;

    @Schema(description = "Tổng doanh thu tất cả các nhóm (VND)", example = "15000000")
    private BigDecimal totalRevenue;

    @Schema(description = "Khoảng thời gian thống kê (7, 30, 90 ngày)", example = "30")
    private Integer periodDays;

    /**
     * DTO cho doanh thu từng nhóm món
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Doanh thu theo danh mục")
    public static class CategoryRevenue {
        @Schema(description = "ID danh mục (null nếu là nhóm 'Khác')", example = "1")
        private Long categoryId;

        @Schema(description = "Tên danh mục", example = "Món chính")
        private String categoryName;

        @Schema(description = "Slug danh mục", example = "mon-chinh")
        private String categorySlug;

        @Schema(description = "Doanh thu của danh mục (VND)", example = "5000000")
        private BigDecimal revenue;

        @Schema(description = "Tỷ lệ % doanh thu so với tổng", example = "33.3")
        private Double percentage;

        @Schema(description = "Số lượng đơn hàng có món thuộc danh mục này", example = "100")
        private Long orderCount;

        @Schema(description = "Số lượng món đã bán thuộc danh mục này", example = "200")
        private Long quantitySold;

        @Schema(description = "Màu sắc cho biểu đồ", example = "#FF6384")
        private String color;
    }
}
