package com.foodorder.backend.repository;

import com.foodorder.backend.entity.ChangePasswordAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ChangePasswordAttemptRepository extends JpaRepository<ChangePasswordAttempt, Long> {
    int countByUserIdAndAttemptedAtAfter(Long userId, LocalDateTime after);
}
