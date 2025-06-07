package com.foodorder.backend.repository;

import com.foodorder.backend.entity.ForgotPasswordRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface ForgotPasswordRequestRepository extends JpaRepository<ForgotPasswordRequest, Long> {
    int countByEmailAndRequestedAtAfter(String email, LocalDateTime after);
}