package com.foodorder.backend.dashboard.dto.response;

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
public class RevenueByCategoryResponse {

    /**
     * Danh sách cơ cấu doanh thu (3 nhóm chính + 1 nhóm Khác)
     */
    private List<CategoryRevenue> categories;

    /**
     * Tổng doanh thu tất cả các nhóm
     */
    private BigDecimal totalRevenue;

    /**
     * Khoảng thời gian thống kê (7, 30, 90 ngày)
     */
    private Integer periodDays;

    /**
     * DTO cho doanh thu từng nhóm món
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryRevenue {
        /**
         * ID danh mục (null nếu là nhóm "Khác")
         */
        private Long categoryId;

        /**
         * Tên danh mục
         */
        private String categoryName;

        /**
         * Slug danh mục
         */
        private String categorySlug;

        /**
         * Doanh thu của danh mục
         */
        private BigDecimal revenue;

        /**
         * Tỷ lệ % doanh thu so với tổng
         */
        private Double percentage;

        /**
         * Số lượng đơn hàng có món thuộc danh mục này
         */
        private Long orderCount;

        /**
         * Số lượng món đã bán thuộc danh mục này
         */
        private Long quantitySold;

        /**
         * Màu sắc cho biểu đồ (tùy chọn)
         */
        private String color;
    }
}

