package com.foodorder.backend.dashboard.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Response chứa chi tiết hiệu quả từng món ăn")
public class FoodPerformanceResponse {

    @Schema(description = "Danh sách hiệu quả từng món ăn")
    private List<FoodPerformanceItem> foods;

    @Schema(description = "Tổng số món ăn", example = "50")
    private Integer totalFoods;

    @Schema(description = "Trang hiện tại", example = "0")
    private Integer currentPage;

    @Schema(description = "Tổng số trang", example = "5")
    private Integer totalPages;

    @Schema(description = "Khoảng thời gian thống kê (7, 30, 90 ngày)", example = "30")
    private Integer periodDays;

    /**
     * DTO cho hiệu quả từng món ăn
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Thông tin hiệu quả một món ăn")
    public static class FoodPerformanceItem {
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

        @Schema(description = "Số đơn hàng có món này", example = "100")
        private Long orderCount;

        @Schema(description = "Số lượng đã bán", example = "150")
        private Long quantitySold;

        @Schema(description = "Doanh thu từ món này (VND)", example = "8250000")
        private BigDecimal revenue;

        @Schema(description = "Điểm đánh giá trung bình (0-5)", example = "4.5")
        private Double averageRating;

        @Schema(description = "Số lượng đánh giá", example = "50")
        private Long reviewCount;

        @Schema(description = "Xu hướng so với kỳ trước", example = "UP", allowableValues = {"UP", "DOWN", "STABLE", "NEW"})
        private TrendType trend;

        @Schema(description = "Tỷ lệ thay đổi doanh thu so với kỳ trước (%)", example = "15.5")
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
