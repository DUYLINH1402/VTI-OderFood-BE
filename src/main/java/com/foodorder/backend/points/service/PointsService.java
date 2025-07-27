package com.foodorder.backend.points.service;

import com.foodorder.backend.points.dto.response.PointsResponseDTO;
import com.foodorder.backend.points.dto.response.PointsHistoryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PointsService {
    // Cộng điểm và lưu log khi hoàn thành đơn hàng, khuyến mãi, v.v.
    void addPointsOnOrder(Long userId, Long orderId, int amount, String description);

    // Lấy điểm hiện tại của user theo username
    PointsResponseDTO getCurrentPointsByUsername(String username);

    // Lấy lịch sử sử dụng điểm của user theo username (hỗ trợ phân trang)
    Page<PointsHistoryDTO> getPointsHistoryByUsername(String username, Pageable pageable);

    // Trừ điểm và lưu log khi thanh toán đơn hàng
    void usePointsOnOrder(Long userId, Long orderId, int discountAmount, String description);
}
