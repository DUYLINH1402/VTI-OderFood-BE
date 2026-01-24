package com.foodorder.backend.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO cho API Top món ăn bán chạy
 * Chứa danh sách 5 món bán chạy nhất theo khoảng thời gian
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response chứa danh sách top món ăn bán chạy")
public class TopSellingFoodResponse {

    @Schema(description = "Danh sách top món ăn bán chạy")
    private List<TopFoodItem> topFoods;

    @Schema(description = "Tổng doanh thu của các món bán chạy (VND)", example = "5000000")
    private BigDecimal totalRevenue;

    @Schema(description = "Tổng số lượng bán của các món bán chạy", example = "500")
    private Long totalQuantitySold;

    @Schema(description = "Khoảng thời gian thống kê (7, 30, 90 ngày)", example = "30")
    private Integer periodDays;

    /**
     * DTO cho từng món ăn bán chạy
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Thông tin món ăn bán chạy")
    public static class TopFoodItem {
        @Schema(description = "ID món ăn", example = "1")
        private Long foodId;

        @Schema(description = "Tên món ăn", example = "Phở bò tái")
        private String foodName;

        @Schema(description = "Slug món ăn", example = "pho-bo-tai")
        private String foodSlug;

        @Schema(description = "URL ảnh đại diện", example = "https://example.com/pho.jpg")
        private String imageUrl;

        @Schema(description = "Tên danh mục", example = "Món chính")
        private String categoryName;

        @Schema(description = "Số lượng đã bán", example = "150")
        private Long quantitySold;

        @Schema(description = "Doanh thu từ món này (VND)", example = "8250000")
        private BigDecimal revenue;

        @Schema(description = "Tỷ lệ % so với tổng doanh thu top 5", example = "25.5")
        private Double revenuePercentage;
    }
}

