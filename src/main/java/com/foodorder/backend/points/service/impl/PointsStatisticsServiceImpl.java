package com.foodorder.backend.points.service.impl;

import com.foodorder.backend.exception.ResourceNotFoundException;
import com.foodorder.backend.points.dto.response.*;
import com.foodorder.backend.points.entity.PointHistory;
import com.foodorder.backend.points.entity.PointType;
import com.foodorder.backend.points.entity.RewardPoint;
import com.foodorder.backend.points.repository.PointHistoryRepository;
import com.foodorder.backend.points.repository.RewardPointRepository;
import com.foodorder.backend.points.service.PointsStatisticsService;
import com.foodorder.backend.user.entity.User;
import com.foodorder.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation cho PointsStatisticsService
 * Xử lý các nghiệp vụ thống kê và phân tích điểm thưởng
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PointsStatisticsServiceImpl implements PointsStatisticsService {

    private final PointHistoryRepository pointHistoryRepository;
    private final RewardPointRepository rewardPointRepository;
    private final UserRepository userRepository;

    // === THỐNG KÊ TỔNG QUAN ===

    @Override
    @Cacheable(value = "pointsStatisticsCache", key = "'overall'")
    public PointsStatisticsResponse getOverallStatistics() {

        // Tổng quan hệ thống
        Long totalUsersWithPoints = rewardPointRepository.countUsersWithPoints();
        Long totalPointsInSystem = rewardPointRepository.getTotalPointsInSystem();
        Double averagePointsPerUser = rewardPointRepository.getAveragePointsPerUser();

        // Thống kê theo loại
        Long totalPointsEarned = pointHistoryRepository.sumPointsByType(PointType.EARN);
        Long totalPointsUsed = pointHistoryRepository.sumPointsByType(PointType.USE);
        Long totalPointsRefunded = pointHistoryRepository.sumPointsByType(PointType.REFUND);
        Long totalPointsExpired = pointHistoryRepository.sumPointsByType(PointType.EXPIRE);

        // Đếm giao dịch theo loại
        Map<String, Long> pointsByType = new HashMap<>();
        Map<String, Long> transactionsByType = new HashMap<>();
        for (PointType type : PointType.values()) {
            pointsByType.put(type.name(), pointHistoryRepository.sumPointsByType(type));
            transactionsByType.put(type.name(), pointHistoryRepository.countByType(type));
        }

        // Tính trung bình
        Long earnTransactions = transactionsByType.getOrDefault(PointType.EARN.name(), 0L);
        Long useTransactions = transactionsByType.getOrDefault(PointType.USE.name(), 0L);
        double avgEarnedPerOrder = earnTransactions > 0 ? (double) totalPointsEarned / earnTransactions : 0;
        double avgUsedPerOrder = useTransactions > 0 ? (double) totalPointsUsed / useTransactions : 0;

        // Tính tỷ lệ
        double usageRate = totalPointsEarned > 0 ? (double) totalPointsUsed / totalPointsEarned * 100 : 0;
        double retentionRate = 100 - usageRate;

        return PointsStatisticsResponse.builder()
                .totalUsersWithPoints(totalUsersWithPoints)
                .totalPointsInSystem(totalPointsInSystem)
                .totalPointsEarned(totalPointsEarned)
                .totalPointsUsed(totalPointsUsed)
                .totalPointsRefunded(totalPointsRefunded)
                .totalPointsExpired(totalPointsExpired)
                .averagePointsPerUser(Math.round(averagePointsPerUser * 100.0) / 100.0)
                .averagePointsEarnedPerOrder(Math.round(avgEarnedPerOrder * 100.0) / 100.0)
                .averagePointsUsedPerOrder(Math.round(avgUsedPerOrder * 100.0) / 100.0)
                .pointsByType(pointsByType)
                .transactionsByType(transactionsByType)
                .usageRate(Math.round(usageRate * 100.0) / 100.0)
                .retentionRate(Math.round(retentionRate * 100.0) / 100.0)
                .build();
    }

    // === PHÂN TÍCH XU HƯỚNG ===

    @Override
    public PointsTrendAnalyticsResponse getTrendAnalytics(LocalDateTime startDate, LocalDateTime endDate) {

        // Thống kê trong khoảng thời gian
        Long totalEarned = pointHistoryRepository.sumPointsByTypeAndDateRange(PointType.EARN, startDate, endDate);
        Long totalUsed = pointHistoryRepository.sumPointsByTypeAndDateRange(PointType.USE, startDate, endDate);
        Long netChange = totalEarned - totalUsed;
        Long totalTransactions = pointHistoryRepository.countByDateRange(startDate, endDate);
        Long uniqueUsersEarned = pointHistoryRepository.countUniqueUsersByTypeAndDateRange(PointType.EARN, startDate, endDate);
        Long uniqueUsersUsed = pointHistoryRepository.countUniqueUsersByTypeAndDateRange(PointType.USE, startDate, endDate);

        // Xu hướng theo ngày
        List<PointsTrendAnalyticsResponse.DailyPointsData> dailyTrend = getDailyTrend(startDate, endDate);

        // So sánh với kỳ trước
        long periodDays = ChronoUnit.DAYS.between(startDate, endDate);
        LocalDateTime previousStart = startDate.minusDays(periodDays);
        LocalDateTime previousEnd = startDate;

        Long previousEarned = pointHistoryRepository.sumPointsByTypeAndDateRange(PointType.EARN, previousStart, previousEnd);
        Long previousUsed = pointHistoryRepository.sumPointsByTypeAndDateRange(PointType.USE, previousStart, previousEnd);

        double earnedChangePercent = previousEarned > 0 ?
                ((double) totalEarned - previousEarned) / previousEarned * 100 : 0;
        double usedChangePercent = previousUsed > 0 ?
                ((double) totalUsed - previousUsed) / previousUsed * 100 : 0;

        PointsTrendAnalyticsResponse.TrendComparison comparison = PointsTrendAnalyticsResponse.TrendComparison.builder()
                .previousPeriodEarned(previousEarned)
                .previousPeriodUsed(previousUsed)
                .earnedChangePercent(Math.round(earnedChangePercent * 100.0) / 100.0)
                .usedChangePercent(Math.round(usedChangePercent * 100.0) / 100.0)
                .build();

        return PointsTrendAnalyticsResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalPointsEarned(totalEarned)
                .totalPointsUsed(totalUsed)
                .netPointsChange(netChange)
                .totalTransactions(totalTransactions)
                .uniqueUsersEarned(uniqueUsersEarned)
                .uniqueUsersUsed(uniqueUsersUsed)
                .dailyTrend(dailyTrend)
                .comparison(comparison)
                .build();
    }

    @Override
    public List<PointsTrendAnalyticsResponse.DailyPointsData> getDailyTrend(LocalDateTime startDate,
                                                                              LocalDateTime endDate) {

        List<Object[]> results = pointHistoryRepository.getDailyPointsStatistics(startDate, endDate);

        // Group by date
        Map<String, PointsTrendAnalyticsResponse.DailyPointsData> dataMap = new LinkedHashMap<>();

        for (Object[] row : results) {
            String date = row[0].toString();
            PointType type = (PointType) row[1];
            long points = ((Number) row[2]).longValue();
            long count = ((Number) row[3]).longValue();

            PointsTrendAnalyticsResponse.DailyPointsData data = dataMap.computeIfAbsent(date,
                    d -> PointsTrendAnalyticsResponse.DailyPointsData.builder()
                            .date(d)
                            .pointsEarned(0L)
                            .pointsUsed(0L)
                            .netChange(0L)
                            .transactionCount(0L)
                            .build());

            if (type == PointType.EARN) {
                data.setPointsEarned(data.getPointsEarned() + points);
            } else if (type == PointType.USE) {
                data.setPointsUsed(data.getPointsUsed() + points);
            }
            data.setTransactionCount(data.getTransactionCount() + count);
            data.setNetChange(data.getPointsEarned() - data.getPointsUsed());
        }

        return new ArrayList<>(dataMap.values());
    }

    // === BÁO CÁO THEO USER ===

    @Override
    public UserPointsDetailResponse getUserPointsDetail(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "Không tìm thấy người dùng"));

        RewardPoint rewardPoint = rewardPointRepository.findByUser(user).orElse(null);

        // Thống kê tổng quan
        Long totalEarned = pointHistoryRepository.sumPointsByUserIdAndType(userId, PointType.EARN);
        Long totalUsed = pointHistoryRepository.sumPointsByUserIdAndType(userId, PointType.USE);
        Long totalRefunded = pointHistoryRepository.sumPointsByUserIdAndType(userId, PointType.REFUND);
        Long totalTransactions = pointHistoryRepository.countByUserId(userId);

        // Lấy lịch sử gần đây
        Pageable pageable = PageRequest.of(0, 20);
        List<PointHistory> recentHistory = pointHistoryRepository.findRecentByUserId(userId, pageable);

        List<UserPointsDetailResponse.PointTransactionDetail> transactions = recentHistory.stream()
                .map(ph -> UserPointsDetailResponse.PointTransactionDetail.builder()
                        .transactionId(ph.getId())
                        .type(ph.getType().name())
                        .amount(ph.getAmount())
                        .orderId(ph.getOrderId())
                        .description(ph.getDescription())
                        .createdAt(ph.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return UserPointsDetailResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .currentBalance(rewardPoint != null ? rewardPoint.getBalance() : 0)
                .lastUpdated(rewardPoint != null ? rewardPoint.getLastUpdated() : null)
                .totalPointsEarned(totalEarned)
                .totalPointsUsed(totalUsed)
                .totalPointsRefunded(totalRefunded)
                .totalTransactions(totalTransactions)
                .recentTransactions(transactions)
                .build();
    }

    @Override
    public List<TopUserByPointsResponse> getTopUsersByPoints(int limit) {

        Pageable pageable = PageRequest.of(0, limit);
        List<RewardPoint> topUsers = rewardPointRepository.findTopByBalance(pageable);

        List<TopUserByPointsResponse> result = new ArrayList<>();
        int rank = 1;

        for (RewardPoint rp : topUsers) {
            User user = rp.getUser();
            Long totalEarned = pointHistoryRepository.sumPointsByUserIdAndType(user.getId(), PointType.EARN);
            Long totalUsed = pointHistoryRepository.sumPointsByUserIdAndType(user.getId(), PointType.USE);

            result.add(TopUserByPointsResponse.builder()
                    .rank(rank++)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .fullName(user.getFullName())
                    .email(user.getEmail())
                    .currentBalance(rp.getBalance())
                    .totalPointsEarned(totalEarned)
                    .totalPointsUsed(totalUsed)
                    .build());
        }

        return result;
    }

    @Override
    public Page<UserPointsDetailResponse> getUsersWithPoints(int minBalance, Pageable pageable) {

        Page<RewardPoint> rewardPoints = rewardPointRepository.findByBalanceGreaterThanEqual(minBalance, pageable);

        List<UserPointsDetailResponse> users = rewardPoints.getContent().stream()
                .map(rp -> getUserPointsDetail(rp.getUser().getId()))
                .collect(Collectors.toList());

        return new PageImpl<>(users, pageable, rewardPoints.getTotalElements());
    }

    // === QUẢN LÝ ĐIỂM (ADMIN) ===

    @Override
    @Transactional
    public void adjustUserPoints(Long userId, int amount, String reason) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "Không tìm thấy người dùng"));

        RewardPoint rewardPoint = rewardPointRepository.findByUser(user)
                .orElseGet(() -> {
                    RewardPoint newRp = RewardPoint.builder()
                            .user(user)
                            .balance(0)
                            .lastUpdated(LocalDateTime.now())
                            .build();
                    return rewardPointRepository.save(newRp);
                });

        // Cập nhật số dư
        int newBalance = rewardPoint.getBalance() + amount;
        if (newBalance < 0) {
            newBalance = 0;
        }
        rewardPoint.setBalance(newBalance);
        rewardPoint.setLastUpdated(LocalDateTime.now());
        rewardPointRepository.save(rewardPoint);

        // Ghi lịch sử
        PointType type = amount >= 0 ? PointType.EARN : PointType.USE;
        PointHistory history = PointHistory.builder()
                .userId(userId)
                .type(type)
                .amount(Math.abs(amount))
                .description("[ADMIN] " + reason)
                .createdAt(LocalDateTime.now())
                .build();
        pointHistoryRepository.save(history);

    }

    @Override
    @Transactional
    public void bulkAddPoints(List<Long> userIds, int amount, String reason) {

        for (Long userId : userIds) {
            try {
                adjustUserPoints(userId, amount, reason);
            } catch (Exception e) {
                log.error("Lỗi khi cộng điểm cho user {}: {}", userId, e.getMessage());
            }
        }

    }
}

