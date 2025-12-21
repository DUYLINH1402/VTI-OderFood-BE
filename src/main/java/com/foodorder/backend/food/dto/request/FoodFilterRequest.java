package com.foodorder.backend.food.dto.request;

import lombok.*;

/**
 * DTO dùng để lọc danh sách món ăn trong trang quản lý
 * Hỗ trợ lọc theo tên, trạng thái, danh mục, và trạng thái hoạt động
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodFilterRequest {

    // Lọc theo tên món ăn (tìm kiếm gần đúng)
    private String name;

    // Lọc theo trạng thái (AVAILABLE, UNAVAILABLE)
    private String status;

    // Lọc theo ID danh mục
    private Long categoryId;

    // Lọc theo trạng thái hoạt động
    private Boolean isActive;
}

