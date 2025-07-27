package com.foodorder.backend.points.repository;

import com.foodorder.backend.points.entity.RewardPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import com.foodorder.backend.user.entity.User;

public interface RewardPointRepository extends JpaRepository<RewardPoint, Long> {
    Optional<RewardPoint> findByUser(User user);
}
