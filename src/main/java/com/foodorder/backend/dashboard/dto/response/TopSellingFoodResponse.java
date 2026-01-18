package com.foodorder.backend.dashboard.dto.response;

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
public class TopSellingFoodResponse {

    /**
     * Danh sách top món ăn bán chạy
     */
    private List<TopFoodItem> topFoods;

    /**
     * Tổng doanh thu của các món bán chạy
     */
    private BigDecimal totalRevenue;

    /**
     * Tổng số lượng bán của các món bán chạy
     */
    private Long totalQuantitySold;

    /**
     * Khoảng thời gian thống kê (7, 30, 90 ngày)
     */
    private Integer periodDays;

    /**
     * DTO cho từng món ăn bán chạy
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopFoodItem {
        /**
         * ID món ăn
         */
        private Long foodId;

        /**
         * Tên món ăn
         */
        private String foodName;

        /**
         * Slug món ăn
         */
        private String foodSlug;

        /**
         * Ảnh đại diện
         */
        private String imageUrl;

        /**
         * Tên danh mục
         */
        private String categoryName;

        /**
         * Số lượng đã bán
         */
        private Long quantitySold;

        /**
         * Doanh thu từ món này
         */
        private BigDecimal revenue;

        /**
         * Tỷ lệ % so với tổng doanh thu top 5
         */
        private Double revenuePercentage;
    }
}

