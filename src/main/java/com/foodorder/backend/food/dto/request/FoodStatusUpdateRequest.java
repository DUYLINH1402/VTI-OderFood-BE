package com.foodorder.backend.food.dto.request;

import lombok.*;

/**
 * DTO dùng để thay đổi trạng thái món ăn
 * Staff có thể thay đổi status (AVAILABLE/UNAVAILABLE) hoặc isActive
 * Có thể thêm ghi chú lý do hết hàng
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodStatusUpdateRequest {

    // Trạng thái món ăn: AVAILABLE hoặc UNAVAILABLE
    private String status;

    // Trạng thái hoạt động của món ăn
    private Boolean isActive;

    // Ghi chú lý do thay đổi trạng thái (VD: hết nguyên liệu, bảo trì...)
    private String statusNote;
}
