package com.foodorder.backend.points.repository;

import com.foodorder.backend.points.entity.RewardPoint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

import com.foodorder.backend.user.entity.User;

@Repository
public interface RewardPointRepository extends JpaRepository<RewardPoint, Long> {
    Optional<RewardPoint> findByUser(User user);

    // === THỐNG KÊ NÂNG CAO ===

    /**
     * Tính tổng điểm trong hệ thống
     */
    @Query("SELECT COALESCE(SUM(rp.balance), 0) FROM RewardPoint rp")
    Long getTotalPointsInSystem();

    /**
     * Đếm số user có điểm > 0
     */
    @Query("SELECT COUNT(rp) FROM RewardPoint rp WHERE rp.balance > 0")
    Long countUsersWithPoints();

    /**
     * Tính điểm trung bình mỗi user
     */
    @Query("SELECT COALESCE(AVG(rp.balance), 0) FROM RewardPoint rp")
    Double getAveragePointsPerUser();

    /**
     * Lấy top user có nhiều điểm nhất
     */
    @Query("SELECT rp FROM RewardPoint rp ORDER BY rp.balance DESC")
    List<RewardPoint> findTopByBalance(Pageable pageable);

    /**
     * Lấy danh sách user có điểm với phân trang (có filter)
     */
    @Query("SELECT rp FROM RewardPoint rp WHERE rp.balance >= :minBalance ORDER BY rp.balance DESC")
    Page<RewardPoint> findByBalanceGreaterThanEqual(int minBalance, Pageable pageable);
}
