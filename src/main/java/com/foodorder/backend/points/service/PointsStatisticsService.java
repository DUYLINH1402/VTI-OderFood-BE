package com.foodorder.backend.points.service;

import com.foodorder.backend.points.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface cho thống kê và phân tích điểm thưởng
 * Cung cấp các phương thức thống kê nâng cao cho admin dashboard
 */
public interface PointsStatisticsService {

    // === THỐNG KÊ TỔNG QUAN ===

    /**
     * Lấy thống kê tổng quan về điểm thưởng
     */
    PointsStatisticsResponse getOverallStatistics();

    // === PHÂN TÍCH XU HƯỚNG ===

    /**
     * Phân tích xu hướng điểm thưởng trong khoảng thời gian
     */
    PointsTrendAnalyticsResponse getTrendAnalytics(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Lấy xu hướng điểm theo ngày
     */
    List<PointsTrendAnalyticsResponse.DailyPointsData> getDailyTrend(LocalDateTime startDate,
                                                                      LocalDateTime endDate);

    // === BÁO CÁO THEO USER ===

    /**
     * Lấy thông tin chi tiết điểm thưởng của một user
     */
    UserPointsDetailResponse getUserPointsDetail(Long userId);

    /**
     * Lấy top user có điểm thưởng cao nhất
     */
    List<TopUserByPointsResponse> getTopUsersByPoints(int limit);

    /**
     * Lấy danh sách user có điểm với phân trang và filter
     */
    Page<UserPointsDetailResponse> getUsersWithPoints(int minBalance, Pageable pageable);

    // === QUẢN LÝ ĐIỂM (ADMIN) ===

    /**
     * Điều chỉnh điểm cho user (admin)
     * @param userId ID của user
     * @param amount Số điểm điều chỉnh (dương: cộng, âm: trừ)
     * @param reason Lý do điều chỉnh
     */
    void adjustUserPoints(Long userId, int amount, String reason);

    /**
     * Cộng điểm thưởng cho nhiều user (campaign)
     */
    void bulkAddPoints(List<Long> userIds, int amount, String reason);
}

