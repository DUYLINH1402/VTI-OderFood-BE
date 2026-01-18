package com.foodorder.backend.coupons.service.impl;

import com.foodorder.backend.coupons.dto.response.*;
import com.foodorder.backend.coupons.entity.*;
import com.foodorder.backend.coupons.repository.CouponRepository;
import com.foodorder.backend.coupons.repository.CouponUsageRepository;
import com.foodorder.backend.coupons.service.CouponStatisticsService;
import com.foodorder.backend.exception.ResourceNotFoundException;
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
 * Implementation cho CouponStatisticsService
 * Xử lý các nghiệp vụ thống kê và phân tích coupon
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CouponStatisticsServiceImpl implements CouponStatisticsService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final UserRepository userRepository;

    // === THỐNG KÊ TỔNG QUAN ===

    @Override
    @Cacheable(value = "couponStatisticsCache", key = "'overall'")
    public CouponStatisticsResponse getOverallStatistics() {

        // Đếm theo trạng thái
        long totalCoupons = couponRepository.count();
        long activeCoupons = couponRepository.countByStatus(CouponStatus.ACTIVE);
        long expiredCoupons = couponRepository.countByStatus(CouponStatus.EXPIRED);
        long inactiveCoupons = couponRepository.countByStatus(CouponStatus.INACTIVE);
        long usedOutCoupons = couponRepository.findUsedOutCoupons().size();

        // Thống kê sử dụng
        long totalUsageCount = couponUsageRepository.count();
        Double totalDiscountAmount = couponUsageRepository.getTotalDiscountAmount();
        double avgDiscount = totalUsageCount > 0 ? totalDiscountAmount / totalUsageCount : 0;

        // Phân bổ theo loại
        Map<String, Long> couponsByType = new HashMap<>();
        for (CouponType type : CouponType.values()) {
            couponsByType.put(type.name(), couponRepository.countByCouponType(type));
        }

        Map<String, Long> couponsByStatus = new HashMap<>();
        for (CouponStatus status : CouponStatus.values()) {
            couponsByStatus.put(status.name(), couponRepository.countByStatus(status));
        }

        Map<String, Long> couponsByDiscountType = new HashMap<>();
        for (DiscountType type : DiscountType.values()) {
            couponsByDiscountType.put(type.name(), couponRepository.countByDiscountType(type));
        }

        // Tính tỷ lệ
        double activeRate = totalCoupons > 0 ? (double) activeCoupons / totalCoupons * 100 : 0;
        Long maxUsageSum = couponRepository.getTotalUsageCount();
        double usageRate = maxUsageSum > 0 ? (double) totalUsageCount / maxUsageSum * 100 : 0;

        return CouponStatisticsResponse.builder()
                .totalCoupons(totalCoupons)
                .activeCoupons(activeCoupons)
                .expiredCoupons(expiredCoupons)
                .inactiveCoupons(inactiveCoupons)
                .usedOutCoupons(usedOutCoupons)
                .totalUsageCount(totalUsageCount)
                .totalDiscountAmount(totalDiscountAmount)
                .averageDiscountAmount(Math.round(avgDiscount * 100.0) / 100.0)
                .couponsByType(couponsByType)
                .couponsByStatus(couponsByStatus)
                .couponsByDiscountType(couponsByDiscountType)
                .usageRate(Math.round(usageRate * 100.0) / 100.0)
                .activeRate(Math.round(activeRate * 100.0) / 100.0)
                .build();
    }

    // === PHÂN TÍCH SỬ DỤNG ===

    @Override
    public CouponUsageAnalyticsResponse getUsageAnalytics(LocalDateTime startDate, LocalDateTime endDate) {

        // Thống kê trong khoảng thời gian
        Long usageCount = couponUsageRepository.countByDateRange(startDate, endDate);
        Double totalDiscount = couponUsageRepository.getTotalDiscountAmountByDateRange(startDate, endDate);
        Long uniqueUsers = couponUsageRepository.countUniqueUsersByDateRange(startDate, endDate);
        double avgDiscount = usageCount > 0 ? totalDiscount / usageCount : 0;

        // Xu hướng theo ngày
        List<CouponUsageAnalyticsResponse.DailyUsageData> dailyData = getDailyUsageData(startDate, endDate);

        // Top coupon
        List<CouponUsageAnalyticsResponse.TopCouponData> topByUsage = getTopCoupons("USAGE", 10);
        List<CouponUsageAnalyticsResponse.TopCouponData> topByDiscount = getTopCoupons("DISCOUNT", 10);

        // Phân tích theo loại
        Map<String, CouponUsageAnalyticsResponse.UsageByTypeData> usageByType = getUsageByType(startDate, endDate);

        return CouponUsageAnalyticsResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .usageCount(usageCount)
                .totalDiscountAmount(totalDiscount)
                .averageDiscountPerUsage(Math.round(avgDiscount * 100.0) / 100.0)
                .uniqueUsersCount(uniqueUsers)
                .dailyUsageData(dailyData)
                .topCouponsByUsage(topByUsage)
                .topCouponsByDiscount(topByDiscount)
                .usageByType(usageByType)
                .build();
    }

    @Override
    public CouponPerformanceResponse getCouponPerformance(Long couponId) {

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("COUPON_NOT_FOUND", "Không tìm thấy coupon"));

        // Tính số ngày
        LocalDateTime now = LocalDateTime.now();
        long daysActive = ChronoUnit.DAYS.between(coupon.getStartDate(),
                now.isBefore(coupon.getEndDate()) ? now : coupon.getEndDate());
        long daysRemaining = now.isBefore(coupon.getEndDate()) ?
                ChronoUnit.DAYS.between(now, coupon.getEndDate()) : 0;

        // Thống kê sử dụng
        int remainingUsage = coupon.getMaxUsage() - coupon.getUsedCount();
        double usageRate = coupon.getMaxUsage() > 0 ?
                (double) coupon.getUsedCount() / coupon.getMaxUsage() * 100 : 0;

        // Thống kê từ usage history
        Double totalDiscount = couponUsageRepository.getTotalDiscountAmountByCoupon(coupon);
        Long uniqueUsers = couponUsageRepository.countUniqueUsersByCoupon(coupon);
        double avgDiscount = coupon.getUsedCount() > 0 ? totalDiscount / coupon.getUsedCount() : 0;
        double avgUsagePerDay = daysActive > 0 ? (double) coupon.getUsedCount() / daysActive : 0;

        return CouponPerformanceResponse.builder()
                .couponId(coupon.getId())
                .code(coupon.getCode())
                .title(coupon.getTitle())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType().name())
                .discountValue(coupon.getDiscountValue())
                .couponType(coupon.getCouponType().name())
                .status(coupon.getStatus().name())
                .startDate(coupon.getStartDate())
                .endDate(coupon.getEndDate())
                .daysActive(daysActive)
                .daysRemaining(daysRemaining)
                .maxUsage(coupon.getMaxUsage())
                .usedCount(coupon.getUsedCount())
                .remainingUsage(remainingUsage)
                .usageRate(Math.round(usageRate * 100.0) / 100.0)
                .totalDiscountAmount(totalDiscount)
                .averageDiscountAmount(Math.round(avgDiscount * 100.0) / 100.0)
                .uniqueUsersCount(uniqueUsers)
                .averageUsagePerDay(Math.round(avgUsagePerDay * 100.0) / 100.0)
                .minOrderAmount(coupon.getMinOrderAmount())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .maxUsagePerUser(coupon.getMaxUsagePerUser())
                .build();
    }

    @Override
    public List<CouponUsageAnalyticsResponse.TopCouponData> getTopCoupons(String criteria, int limit) {

        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> results;

        if ("DISCOUNT".equalsIgnoreCase(criteria)) {
            results = couponUsageRepository.getTopCouponsByDiscountAmount(pageable);
        } else {
            // Mặc định là USAGE
            results = couponRepository.findTopByUsedCount(pageable).stream()
                    .map(c -> new Object[]{c, c.getUsedCount(),
                            couponUsageRepository.getTotalDiscountAmountByCoupon(c)})
                    .collect(Collectors.toList());
        }

        return results.stream()
                .map(row -> {
                    Coupon coupon = (Coupon) row[0];
                    return CouponUsageAnalyticsResponse.TopCouponData.builder()
                            .couponId(coupon.getId())
                            .couponCode(coupon.getCode())
                            .title(coupon.getTitle())
                            .usageCount(((Number) row[1]).longValue())
                            .totalDiscountAmount(((Number) row[2]).doubleValue())
                            .discountType(coupon.getDiscountType().name())
                            .discountValue(coupon.getDiscountValue())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // === BÁO CÁO THEO USER ===

    @Override
    public UserCouponUsageResponse getUserCouponUsage(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND", "Không tìm thấy người dùng"));

        Long totalUsed = couponUsageRepository.countByUser(user);
        Double totalDiscount = couponUsageRepository.getTotalDiscountAmountByUser(user);
        double avgDiscount = totalUsed > 0 ? totalDiscount / totalUsed : 0;

        // Lấy lịch sử sử dụng
        List<CouponUsage> usageHistory = couponUsageRepository.findByUserOrderByUsedAtDesc(user);
        List<UserCouponUsageResponse.CouponUsageDetail> details = usageHistory.stream()
                .map(usage -> UserCouponUsageResponse.CouponUsageDetail.builder()
                        .usageId(usage.getId())
                        .couponId(usage.getCoupon().getId())
                        .couponCode(usage.getCoupon().getCode())
                        .couponTitle(usage.getCoupon().getTitle())
                        .orderId(usage.getOrder().getId())
                        .discountAmount(usage.getDiscountAmount())
                        .usedAt(usage.getUsedAt())
                        .build())
                .collect(Collectors.toList());

        return UserCouponUsageResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .totalCouponsUsed(totalUsed)
                .totalDiscountReceived(totalDiscount)
                .averageDiscountPerOrder(Math.round(avgDiscount * 100.0) / 100.0)
                .usageHistory(details)
                .build();
    }

    @Override
    public List<UserCouponUsageResponse> getTopUsersByCouponUsage(int limit) {

        // Lấy tất cả usage và group theo user
        List<CouponUsage> allUsages = couponUsageRepository.findAll();

        Map<User, List<CouponUsage>> usageByUser = allUsages.stream()
                .collect(Collectors.groupingBy(CouponUsage::getUser));

        return usageByUser.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().size(), e1.getValue().size()))
                .limit(limit)
                .map(entry -> {
                    User user = entry.getKey();
                    List<CouponUsage> usages = entry.getValue();
                    double totalDiscount = usages.stream()
                            .mapToDouble(CouponUsage::getDiscountAmount)
                            .sum();

                    return UserCouponUsageResponse.builder()
                            .userId(user.getId())
                            .username(user.getUsername())
                            .fullName(user.getFullName())
                            .email(user.getEmail())
                            .totalCouponsUsed((long) usages.size())
                            .totalDiscountReceived(totalDiscount)
                            .averageDiscountPerOrder(Math.round(totalDiscount / usages.size() * 100.0) / 100.0)
                            .build();
                })
                .collect(Collectors.toList());
    }

    // === LỌC VÀ TÌM KIẾM NÂNG CAO ===

    @Override
    public Page<CouponPerformanceResponse> filterCoupons(CouponStatus status,
                                                          CouponType couponType,
                                                          String keyword,
                                                          Pageable pageable) {
        Page<Coupon> coupons = couponRepository.findByFilters(status, couponType, keyword, pageable);

        List<CouponPerformanceResponse> performances = coupons.getContent().stream()
                .map(coupon -> getCouponPerformance(coupon.getId()))
                .collect(Collectors.toList());

        return new PageImpl<>(performances, pageable, coupons.getTotalElements());
    }

    // === XU HƯỚNG VÀ DỰ BÁO ===

    @Override
    public List<CouponUsageAnalyticsResponse.DailyUsageData> getUsageTrend(LocalDateTime startDate,
                                                                            LocalDateTime endDate) {
        return getDailyUsageData(startDate, endDate);
    }

    // === PRIVATE HELPER METHODS ===

    private List<CouponUsageAnalyticsResponse.DailyUsageData> getDailyUsageData(LocalDateTime startDate,
                                                                                  LocalDateTime endDate) {
        List<Object[]> results = couponUsageRepository.getDailyUsageStatistics(startDate, endDate);

        return results.stream()
                .map(row -> CouponUsageAnalyticsResponse.DailyUsageData.builder()
                        .date(row[0].toString())
                        .usageCount(((Number) row[1]).longValue())
                        .discountAmount(((Number) row[2]).doubleValue())
                        .build())
                .collect(Collectors.toList());
    }

    private Map<String, CouponUsageAnalyticsResponse.UsageByTypeData> getUsageByType(LocalDateTime startDate,
                                                                                       LocalDateTime endDate) {
        List<Object[]> results = couponUsageRepository.getUsageStatisticsByCouponType(startDate, endDate);

        long totalUsage = results.stream()
                .mapToLong(row -> ((Number) row[1]).longValue())
                .sum();

        Map<String, CouponUsageAnalyticsResponse.UsageByTypeData> map = new HashMap<>();

        for (Object[] row : results) {
            CouponType type = (CouponType) row[0];
            long usageCount = ((Number) row[1]).longValue();
            double discountAmount = ((Number) row[2]).doubleValue();
            double percentage = totalUsage > 0 ? (double) usageCount / totalUsage * 100 : 0;

            map.put(type.name(), CouponUsageAnalyticsResponse.UsageByTypeData.builder()
                    .type(type.name())
                    .usageCount(usageCount)
                    .totalDiscountAmount(discountAmount)
                    .percentage(Math.round(percentage * 100.0) / 100.0)
                    .build());
        }

        return map;
    }
}
