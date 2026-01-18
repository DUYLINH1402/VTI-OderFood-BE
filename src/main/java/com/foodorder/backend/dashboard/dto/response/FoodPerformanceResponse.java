package com.foodorder.backend.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Response DTO cho API chi tiết hiệu quả món ăn
 * Bao gồm: Tên món, Số đơn, Doanh thu, Đánh giá, Xu hướng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodPerformanceResponse {

    /**
     * Danh sách hiệu quả từng món ăn
     */
    private List<FoodPerformanceItem> foods;

    /**
     * Tổng số món ăn
     */
    private Integer totalFoods;

    /**
     * Trang hiện tại
     */
    private Integer currentPage;

    /**
     * Tổng số trang
     */
    private Integer totalPages;

    /**
     * Khoảng thời gian thống kê (7, 30, 90 ngày)
     */
    private Integer periodDays;

    /**
     * DTO cho hiệu quả từng món ăn
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FoodPerformanceItem {
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
         * Số đơn hàng có món này
         */
        private Long orderCount;

        /**
         * Số lượng đã bán
         */
        private Long quantitySold;

        /**
         * Doanh thu từ món này
         */
        private BigDecimal revenue;

        /**
         * Điểm đánh giá trung bình (0-5)
         */
        private Double averageRating;

        /**
         * Số lượng đánh giá
         */
        private Long reviewCount;

        /**
         * Xu hướng: UP (tăng), DOWN (giảm), STABLE (ổn định)
         * So sánh với kỳ trước
         */
        private TrendType trend;

        /**
         * Tỷ lệ thay đổi doanh thu so với kỳ trước (%)
         */
        private Double trendPercentage;
    }

    /**
     * Enum định nghĩa xu hướng
     */
    public enum TrendType {
        UP,      // Tăng trưởng
        DOWN,    // Giảm sút
        STABLE,  // Ổn định
        NEW      // Món mới, chưa có dữ liệu kỳ trước
    }
}

