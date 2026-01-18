package com.foodorder.backend.points.repository;

import com.foodorder.backend.points.entity.PointHistory;
import com.foodorder.backend.points.entity.PointType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {

    Page<PointHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<PointHistory> findByUserIdOrderByCreatedAtAsc(Long userId);

    // ============ DASHBOARD STATISTICS QUERIES ============

    /**
     * Tính tổng điểm đã sử dụng trong khoảng thời gian
     */
    @Query("SELECT COALESCE(SUM(ph.amount), 0) FROM PointHistory ph " +
           "WHERE ph.type = :type " +
           "AND ph.createdAt >= :startDate AND ph.createdAt <= :endDate")
    Long sumPointsByTypeAndDateRange(@Param("type") PointType type,
                                      @Param("startDate") LocalDateTime startDate,
                                      @Param("endDate") LocalDateTime endDate);

    // === THỐNG KÊ NÂNG CAO ===

    /**
     * Tính tổng điểm theo loại (EARN, USE, REFUND, EXPIRE)
     */
    @Query("SELECT COALESCE(SUM(ph.amount), 0) FROM PointHistory ph WHERE ph.type = :type")
    Long sumPointsByType(@Param("type") PointType type);

    /**
     * Đếm số giao dịch theo loại
     */
    Long countByType(PointType type);

    /**
     * Đếm số giao dịch trong khoảng thời gian
     */
    @Query("SELECT COUNT(ph) FROM PointHistory ph WHERE ph.createdAt BETWEEN :startDate AND :endDate")
    Long countByDateRange(@Param("startDate") LocalDateTime startDate,
                          @Param("endDate") LocalDateTime endDate);

    /**
     * Đếm số user duy nhất đã tích điểm trong khoảng thời gian
     */
    @Query("SELECT COUNT(DISTINCT ph.userId) FROM PointHistory ph " +
           "WHERE ph.type = :type AND ph.createdAt BETWEEN :startDate AND :endDate")
    Long countUniqueUsersByTypeAndDateRange(@Param("type") PointType type,
                                             @Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    /**
     * Thống kê điểm theo ngày trong khoảng thời gian
     */
    @Query("SELECT FUNCTION('DATE', ph.createdAt) as date, ph.type, COALESCE(SUM(ph.amount), 0), COUNT(ph) " +
           "FROM PointHistory ph WHERE ph.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE', ph.createdAt), ph.type ORDER BY date")
    List<Object[]> getDailyPointsStatistics(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate);

    /**
     * Lấy lịch sử giao dịch điểm của một user cụ thể (top N records)
     */
    @Query("SELECT ph FROM PointHistory ph WHERE ph.userId = :userId ORDER BY ph.createdAt DESC")
    List<PointHistory> findRecentByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Tính tổng điểm của một user theo loại
     */
    @Query("SELECT COALESCE(SUM(ph.amount), 0) FROM PointHistory ph WHERE ph.userId = :userId AND ph.type = :type")
    Long sumPointsByUserIdAndType(@Param("userId") Long userId, @Param("type") PointType type);

    /**
     * Đếm tổng số giao dịch của một user
     */
    Long countByUserId(Long userId);
}
