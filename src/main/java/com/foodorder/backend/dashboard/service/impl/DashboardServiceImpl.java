package com.foodorder.backend.dashboard.service.impl;

import com.foodorder.backend.dashboard.dto.response.*;
import com.foodorder.backend.dashboard.service.DashboardService;
import com.foodorder.backend.order.entity.Order;
import com.foodorder.backend.order.entity.OrderStatus;
import com.foodorder.backend.order.repository.OrderItemRepository;
import com.foodorder.backend.order.repository.OrderRepository;
import com.foodorder.backend.user.entity.User;
import com.foodorder.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.foodorder.backend.config.CacheConfig.*;

/**
 * Service implementation cho Dashboard Admin
 * Xử lý logic thống kê và lấy dữ liệu cho dashboard
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;

    // Role codes
    private static final String ROLE_USER = "ROLE_USER";
    private static final String ROLE_STAFF = "ROLE_STAFF";

    // Màu sắc cho biểu đồ cơ cấu doanh thu
    private static final String[] CATEGORY_COLORS = {"#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0"};

    @Override
    @Cacheable(value = DASHBOARD_STATISTICS_CACHE, key = "'statistics'")
    public DashboardStatisticsResponse getStatistics() {

        // Lấy thời gian hiện tại
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfToday = now.toLocalDate().atTime(LocalTime.MAX);

        // Tính khoảng thời gian tháng hiện tại
        LocalDateTime startOfMonth = now.withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime endOfMonth = now.toLocalDate().atTime(LocalTime.MAX);

        // Tính khoảng thời gian tháng trước
        LocalDateTime startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDateTime endOfLastMonth = startOfMonth.minusSeconds(1);

        // 1. Tổng số khách hàng
        long totalCustomers = userRepository.countByRoleCode(ROLE_USER
);

        // 2. Tổng số nhân viên
        long totalStaff = userRepository.countByRoleCode(ROLE_STAFF);

        // 3. Doanh thu tháng hiện tại (chỉ tính đơn COMPLETED)
        BigDecimal monthlyRevenue = orderRepository.getTotalRevenueByStatusAndDateRange(
                OrderStatus.COMPLETED, startOfMonth, endOfMonth);
        if (monthlyRevenue == null) {
            monthlyRevenue = BigDecimal.ZERO;
        }

        // 4. Doanh thu tháng trước (để tính tỷ lệ tăng trưởng)
        BigDecimal lastMonthRevenue = orderRepository.getTotalRevenueByStatusAndDateRange(
                OrderStatus.COMPLETED, startOfLastMonth, endOfLastMonth);
        if (lastMonthRevenue == null) {
            lastMonthRevenue = BigDecimal.ZERO;
        }

        // 5. Số đơn hàng hôm nay
        long todayOrders = orderRepository.countOrdersByDateRange(startOfToday, endOfToday);

        // 6. Số đơn hàng đang chờ xử lý (PENDING + PROCESSING)
        long pendingOrders = orderRepository.countByStatusIn(
                Arrays.asList(OrderStatus.PENDING, OrderStatus.PROCESSING));

        // 7. Số đơn hàng hoàn thành hôm nay
        long completedTodayOrders = orderRepository.countOrdersByStatusAndDateRange(
                OrderStatus.COMPLETED, startOfToday, endOfToday);

        // 8. Tính tỷ lệ tăng trưởng doanh thu
        Double revenueGrowthPercent = calculateGrowthPercent(lastMonthRevenue, monthlyRevenue);

        // 9. Số khách hàng mới tháng này và tháng trước để tính tăng trưởng
        long customersThisMonth = userRepository.countNewUsersByRoleAndDateRange(
                ROLE_USER
, startOfMonth, endOfMonth);
        long customersLastMonth = userRepository.countNewUsersByRoleAndDateRange(
                ROLE_USER
, startOfLastMonth, endOfLastMonth);
        Double customerGrowthPercent = calculateGrowthPercentLong(customersLastMonth, customersThisMonth);

        return DashboardStatisticsResponse.builder()
                .totalCustomers(totalCustomers)
                .totalStaff(totalStaff)
                .monthlyRevenue(monthlyRevenue)
                .todayOrders(todayOrders)
                .pendingOrders(pendingOrders)
                .completedTodayOrders(completedTodayOrders)
                .revenueGrowthPercent(revenueGrowthPercent)
                .customerGrowthPercent(customerGrowthPercent)
                .build();
    }

    @Override
    @Cacheable(value = DASHBOARD_REVENUE_CACHE, key = "'revenue_' + #days")
    public RevenueDataResponse getRevenueData(int days) {

        // Validate days
        if (days <= 0) {
            days = 7;
        }
        if (days > 365) {
            days = 365;
        }

        List<RevenueDataResponse.DailyRevenue> dailyRevenues = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        long totalOrders = 0;

        LocalDate today = LocalDate.now();

        // Lấy doanh thu từng ngày
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

            // Lấy doanh thu ngày đó (chỉ tính đơn COMPLETED)
            BigDecimal dayRevenue = orderRepository.getTotalRevenueByStatusAndDateRange(
                    OrderStatus.COMPLETED, startOfDay, endOfDay);
            if (dayRevenue == null) {
                dayRevenue = BigDecimal.ZERO;
            }

            // Đếm số đơn hàng ngày đó
            long dayOrderCount = orderRepository.countOrdersByStatusAndDateRange(
                    OrderStatus.COMPLETED, startOfDay, endOfDay);

            dailyRevenues.add(RevenueDataResponse.DailyRevenue.builder()
                    .date(date)
                    .revenue(dayRevenue)
                    .orderCount(dayOrderCount)
                    .build());

            totalRevenue = totalRevenue.add(dayRevenue);
            totalOrders += dayOrderCount;
        }

        return RevenueDataResponse.builder()
                .dailyRevenues(dailyRevenues)
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .days(days)
                .build();
    }

    @Override
    @Cacheable(value = DASHBOARD_ACTIVITIES_CACHE, key = "'activities_' + #limit")
    public RecentActivityResponse getRecentActivities(int limit) {

        // Validate limit
        if (limit <= 0) {
            limit = 10;
        }
        if (limit > 50) {
            limit = 50;
        }

        List<RecentActivityResponse.Activity> activities = new ArrayList<>();

        // Lấy đơn hàng gần đây
        List<Order> recentOrders = orderRepository.findRecentOrders(PageRequest.of(0, limit));

        for (Order order : recentOrders) {
            String type = getActivityType(order.getStatus());
            String description = getActivityDescription(order);

            activities.add(RecentActivityResponse.Activity.builder()
                    .type(type)
                    .description(description)
                    .timestamp(order.getCreatedAt())
                    .referenceId(order.getId())
                    .orderCode(order.getOrderCode())
                    .customerName(order.getReceiverName())
                    .amount(order.getFinalAmount())
                    .status(order.getStatus().getCode())
                    .build());
        }

        // Lấy khách hàng mới đăng ký gần đây
        List<User> recentCustomers = userRepository.findRecentUsersByRole(
                ROLE_USER
, PageRequest.of(0, limit / 2));

        for (User user : recentCustomers) {
            activities.add(RecentActivityResponse.Activity.builder()
                    .type("USER_REGISTER")
                    .description("Khách hàng mới đăng ký: " + user.getFullName())
                    .timestamp(user.getCreatedAt())
                    .referenceId(user.getId())
                    .customerName(user.getFullName())
                    .build());
        }

        // Sắp xếp theo thời gian mới nhất và giới hạn số lượng
        activities = activities.stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());

        return RecentActivityResponse.builder()
                .activities(activities)
                .totalActivities(activities.size())
                .build();
    }

    /**
     * Tính tỷ lệ tăng trưởng phần trăm
     */
    private Double calculateGrowthPercent(BigDecimal previous, BigDecimal current) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            if (current != null && current.compareTo(BigDecimal.ZERO) > 0) {
                return 100.0; // Tăng 100% nếu trước đó là 0
            }
            return 0.0;
        }

        BigDecimal growth = current.subtract(previous)
                .divide(previous, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        return growth.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * Tính tỷ lệ tăng trưởng phần trăm cho số long
     */
    private Double calculateGrowthPercentLong(long previous, long current) {
        if (previous == 0) {
            if (current > 0) {
                return 100.0;
            }
            return 0.0;
        }

        return ((double) (current - previous) / previous) * 100;
    }

    /**
     * Lấy loại hoạt động dựa trên trạng thái đơn hàng
     */
    private String getActivityType(OrderStatus status) {
        return switch (status) {
            case PENDING, PROCESSING -> "ORDER_NEW";
            case CONFIRMED, DELIVERING -> "ORDER_PROCESSING";
            case COMPLETED -> "ORDER_COMPLETED";
            case CANCELLED -> "ORDER_CANCELLED";
        };
    }

    /**
     * Tạo mô tả hoạt động cho đơn hàng
     */
    private String getActivityDescription(Order order) {
        String customerName = order.getReceiverName();
        String orderCode = order.getOrderCode();

        return switch (order.getStatus()) {
            case PENDING -> "Đơn hàng mới #" + orderCode + " từ " + customerName + " đang chờ thanh toán";
            case PROCESSING -> "Đơn hàng #" + orderCode + " từ " + customerName + " đã thanh toán, chờ xác nhận";
            case CONFIRMED -> "Đơn hàng #" + orderCode + " đang được chế biến";
            case DELIVERING -> "Đơn hàng #" + orderCode + " đang được giao";
            case COMPLETED -> "Đơn hàng #" + orderCode + " đã hoàn thành";
            case CANCELLED -> "Đơn hàng #" + orderCode + " đã bị hủy";
        };
    }

    // ============ ADVANCED STATISTICS IMPLEMENTATION ============

    @Override
    @Cacheable(value = "topSellingFoods", key = "'top_selling_' + #periodDays")
    public TopSellingFoodResponse getTopSellingFoods(int periodDays) {
        // Validate và chuẩn hóa periodDays
        periodDays = validatePeriodDays(periodDays);

        // Tính khoảng thời gian
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(periodDays).toLocalDate().atStartOfDay();

        // Lấy top 5 món bán chạy
        List<Object[]> topFoodsData = orderItemRepository.findTopSellingFoods(
                startDate, endDate, PageRequest.of(0, 5));

        List<TopSellingFoodResponse.TopFoodItem> topFoods = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        Long totalQuantitySold = 0L;

        // Tính tổng doanh thu trước để tính tỷ lệ %
        for (Object[] row : topFoodsData) {
            BigDecimal revenue = row[6] != null ? (BigDecimal) row[6] : BigDecimal.ZERO;
            totalRevenue = totalRevenue.add(revenue);
            totalQuantitySold += row[5] != null ? ((Number) row[5]).longValue() : 0L;
        }

        // Map dữ liệu sang DTO
        for (Object[] row : topFoodsData) {
            Long foodId = row[0] != null ? ((Number) row[0]).longValue() : null;
            String foodName = (String) row[1];
            String foodSlug = (String) row[2];
            String imageUrl = (String) row[3];
            String categoryName = (String) row[4];
            Long quantitySold = row[5] != null ? ((Number) row[5]).longValue() : 0L;
            BigDecimal revenue = row[6] != null ? (BigDecimal) row[6] : BigDecimal.ZERO;

            // Tính tỷ lệ % doanh thu
            Double revenuePercentage = 0.0;
            if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
                revenuePercentage = revenue.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();
            }

            topFoods.add(TopSellingFoodResponse.TopFoodItem.builder()
                    .foodId(foodId)
                    .foodName(foodName)
                    .foodSlug(foodSlug)
                    .imageUrl(imageUrl)
                    .categoryName(categoryName)
                    .quantitySold(quantitySold)
                    .revenue(revenue)
                    .revenuePercentage(revenuePercentage)
                    .build());
        }

        return TopSellingFoodResponse.builder()
                .topFoods(topFoods)
                .totalRevenue(totalRevenue)
                .totalQuantitySold(totalQuantitySold)
                .periodDays(periodDays)
                .build();
    }

    @Override
    @Cacheable(value = "advancedStatistics", key = "'advanced_stats_' + #periodDays")
    public AdvancedStatisticsResponse getAdvancedStatistics(int periodDays) {
        // Validate và chuẩn hóa periodDays
        periodDays = validatePeriodDays(periodDays);

        // Tính khoảng thời gian hiện tại
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(periodDays).toLocalDate().atStartOfDay();

        // Tính khoảng thời gian kỳ trước (để so sánh)
        LocalDateTime prevEndDate = startDate;
        LocalDateTime prevStartDate = prevEndDate.minusDays(periodDays);

        // 1. Tính AOV (Average Order Value) - kỳ hiện tại và kỳ trước
        BigDecimal currentAOV = orderRepository.getAOVByDateRange(startDate, endDate);
        BigDecimal prevAOV = orderRepository.getAOVByDateRange(prevStartDate, prevEndDate);
        Double aovChangePercent = calculateGrowthPercent(prevAOV, currentAOV);

        // 2. Tính tổng đơn hàng và đơn hủy
        Long totalOrders = orderRepository.countOrdersInDateRange(startDate, endDate);
        Long cancelledOrders = orderRepository.countCancelledOrdersByCreatedAtInDateRange(startDate, endDate);

        // Tính tỷ lệ hủy đơn
        Double cancellationRate = 0.0;
        if (totalOrders != null && totalOrders > 0) {
            cancellationRate = (cancelledOrders.doubleValue() / totalOrders.doubleValue()) * 100;
        }

        // Tính tỷ lệ hủy kỳ trước để so sánh
        Long prevTotalOrders = orderRepository.countOrdersInDateRange(prevStartDate, prevEndDate);
        Long prevCancelledOrders = orderRepository.countCancelledOrdersByCreatedAtInDateRange(prevStartDate, prevEndDate);
        Double prevCancellationRate = 0.0;
        if (prevTotalOrders != null && prevTotalOrders > 0) {
            prevCancellationRate = (prevCancelledOrders.doubleValue() / prevTotalOrders.doubleValue()) * 100;
        }
        Double cancellationRateChangePercent = cancellationRate - prevCancellationRate;

        // 3. Tính khách hàng mới
        Long newCustomers = userRepository.countNewUsersByRoleAndDateRange(ROLE_USER, startDate, endDate);
        Long prevNewCustomers = userRepository.countNewUsersByRoleAndDateRange(ROLE_USER, prevStartDate, prevEndDate);
        Double newCustomersChangePercent = calculateGrowthPercentLong(
                prevNewCustomers != null ? prevNewCustomers : 0L,
                newCustomers != null ? newCustomers : 0L);

        // 4. Tính điểm thưởng đã sử dụng
        Long pointsUsed = orderRepository.getTotalPointsUsedInDateRange(startDate, endDate);
        Long prevPointsUsed = orderRepository.getTotalPointsUsedInDateRange(prevStartDate, prevEndDate);
        Double pointsUsedChangePercent = calculateGrowthPercentLong(
                prevPointsUsed != null ? prevPointsUsed : 0L,
                pointsUsed != null ? pointsUsed : 0L);

        // Tính giá trị quy đổi từ điểm
        BigDecimal pointsDiscountValue = orderRepository.getTotalPointsDiscountInDateRange(startDate, endDate);

        return AdvancedStatisticsResponse.builder()
                .aov(currentAOV != null ? currentAOV.setScale(0, RoundingMode.HALF_UP) : BigDecimal.ZERO)
                .aovChangePercent(aovChangePercent)
                .totalOrders(totalOrders != null ? totalOrders : 0L)
                .cancelledOrders(cancelledOrders != null ? cancelledOrders : 0L)
                .cancellationRate(Math.round(cancellationRate * 100.0) / 100.0)
                .cancellationRateChangePercent(Math.round(cancellationRateChangePercent * 100.0) / 100.0)
                .newCustomers(newCustomers != null ? newCustomers : 0L)
                .newCustomersChangePercent(newCustomersChangePercent)
                .pointsUsed(pointsUsed != null ? pointsUsed : 0L)
                .pointsUsedChangePercent(pointsUsedChangePercent)
                .pointsDiscountValue(pointsDiscountValue != null ? pointsDiscountValue : BigDecimal.ZERO)
                .periodDays(periodDays)
                .build();
    }

    @Override
    @Cacheable(value = "revenueByCategory", key = "'revenue_category_' + #periodDays")
    public RevenueByCategoryResponse getRevenueByCategory(int periodDays) {
        // Validate và chuẩn hóa periodDays
        periodDays = validatePeriodDays(periodDays);

        // Tính khoảng thời gian
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(periodDays).toLocalDate().atStartOfDay();

        // Lấy doanh thu theo danh mục
        List<Object[]> categoryRevenueData = orderItemRepository.findRevenueByCategoryInDateRange(startDate, endDate);

        List<RevenueByCategoryResponse.CategoryRevenue> categories = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;

        // Tính tổng doanh thu
        for (Object[] row : categoryRevenueData) {
            BigDecimal revenue = row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO;
            totalRevenue = totalRevenue.add(revenue);
        }

        // Lấy 3 danh mục có doanh thu cao nhất
        int topCategoryCount = Math.min(3, categoryRevenueData.size());
        BigDecimal otherRevenue = BigDecimal.ZERO;
        Long otherOrderCount = 0L;
        Long otherQuantitySold = 0L;

        for (int i = 0; i < categoryRevenueData.size(); i++) {
            Object[] row = categoryRevenueData.get(i);
            Long categoryId = row[0] != null ? ((Number) row[0]).longValue() : null;
            String categoryName = (String) row[1];
            String categorySlug = (String) row[2];
            BigDecimal revenue = row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO;
            Long orderCount = row[4] != null ? ((Number) row[4]).longValue() : 0L;
            Long quantitySold = row[5] != null ? ((Number) row[5]).longValue() : 0L;

            if (i < topCategoryCount) {
                // Thêm vào top 3 danh mục
                Double percentage = 0.0;
                if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
                    percentage = revenue.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP)
                            .doubleValue();
                }

                categories.add(RevenueByCategoryResponse.CategoryRevenue.builder()
                        .categoryId(categoryId)
                        .categoryName(categoryName)
                        .categorySlug(categorySlug)
                        .revenue(revenue)
                        .percentage(percentage)
                        .orderCount(orderCount)
                        .quantitySold(quantitySold)
                        .color(CATEGORY_COLORS[i])
                        .build());
            } else {
                // Gộp vào nhóm "Khác"
                otherRevenue = otherRevenue.add(revenue);
                otherOrderCount += orderCount;
                otherQuantitySold += quantitySold;
            }
        }

        // Thêm nhóm "Khác" nếu có
        if (otherRevenue.compareTo(BigDecimal.ZERO) > 0) {
            Double otherPercentage = 0.0;
            if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
                otherPercentage = otherRevenue.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();
            }

            categories.add(RevenueByCategoryResponse.CategoryRevenue.builder()
                    .categoryId(null)
                    .categoryName("Khác")
                    .categorySlug("other")
                    .revenue(otherRevenue)
                    .percentage(otherPercentage)
                    .orderCount(otherOrderCount)
                    .quantitySold(otherQuantitySold)
                    .color(CATEGORY_COLORS[3])
                    .build());
        }

        return RevenueByCategoryResponse.builder()
                .categories(categories)
                .totalRevenue(totalRevenue)
                .periodDays(periodDays)
                .build();
    }

    @Override
    @Cacheable(value = "foodPerformance", key = "'food_perf_' + #periodDays + '_' + #page + '_' + #size")
    public FoodPerformanceResponse getFoodPerformance(int periodDays, int page, int size) {
        // Validate và chuẩn hóa parameters
        periodDays = validatePeriodDays(periodDays);
        if (page < 0) page = 0;
        if (size <= 0) size = 10;
        if (size > 50) size = 50;

        // Tính khoảng thời gian hiện tại
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(periodDays).toLocalDate().atStartOfDay();

        // Tính khoảng thời gian kỳ trước (để tính xu hướng)
        LocalDateTime prevEndDate = startDate;
        LocalDateTime prevStartDate = prevEndDate.minusDays(periodDays);

        // Lấy dữ liệu hiệu quả món ăn
        List<Object[]> performanceData = orderItemRepository.findFoodPerformanceInDateRange(
                startDate, endDate, PageRequest.of(page, size));

        // Đếm tổng số món ăn
        Long totalFoodsCount = orderItemRepository.countDistinctFoodsInDateRange(startDate, endDate);
        int totalPages = (int) Math.ceil((double) totalFoodsCount / size);

        List<FoodPerformanceResponse.FoodPerformanceItem> foods = new ArrayList<>();

        for (Object[] row : performanceData) {
            Long foodId = row[0] != null ? ((Number) row[0]).longValue() : null;
            String foodName = (String) row[1];
            String foodSlug = (String) row[2];
            String imageUrl = (String) row[3];
            String categoryName = (String) row[4];
            Long orderCount = row[5] != null ? ((Number) row[5]).longValue() : 0L;
            Long quantitySold = row[6] != null ? ((Number) row[6]).longValue() : 0L;
            BigDecimal revenue = row[7] != null ? (BigDecimal) row[7] : BigDecimal.ZERO;

            // Tính xu hướng: so sánh doanh thu với kỳ trước
            BigDecimal prevRevenue = BigDecimal.ZERO;
            if (foodId != null) {
                prevRevenue = orderItemRepository.getFoodRevenueInDateRange(foodId, prevStartDate, prevEndDate);
            }

            FoodPerformanceResponse.TrendType trend;
            Double trendPercentage = 0.0;

            if (prevRevenue == null || prevRevenue.compareTo(BigDecimal.ZERO) == 0) {
                trend = FoodPerformanceResponse.TrendType.NEW;
            } else {
                trendPercentage = calculateGrowthPercent(prevRevenue, revenue);
                if (trendPercentage > 5) {
                    trend = FoodPerformanceResponse.TrendType.UP;
                } else if (trendPercentage < -5) {
                    trend = FoodPerformanceResponse.TrendType.DOWN;
                } else {
                    trend = FoodPerformanceResponse.TrendType.STABLE;
                }
            }

            foods.add(FoodPerformanceResponse.FoodPerformanceItem.builder()
                    .foodId(foodId)
                    .foodName(foodName)
                    .foodSlug(foodSlug)
                    .imageUrl(imageUrl)
                    .categoryName(categoryName)
                    .orderCount(orderCount)
                    .quantitySold(quantitySold)
                    .revenue(revenue)
                    .averageRating(null) // TODO: Thêm khi có hệ thống đánh giá món ăn
                    .reviewCount(0L) // TODO: Thêm khi có hệ thống đánh giá món ăn
                    .trend(trend)
                    .trendPercentage(trendPercentage)
                    .build());
        }

        return FoodPerformanceResponse.builder()
                .foods(foods)
                .totalFoods(totalFoodsCount != null ? totalFoodsCount.intValue() : 0)
                .currentPage(page)
                .totalPages(totalPages)
                .periodDays(periodDays)
                .build();
    }

    /**
     * Validate và chuẩn hóa periodDays
     * Chỉ chấp nhận 7, 30, 90 ngày
     */
    private int validatePeriodDays(int periodDays) {
        return switch (periodDays) {
            case 7, 30, 90 -> periodDays;
            default -> 7; // Mặc định 7 ngày
        };
    }
}
