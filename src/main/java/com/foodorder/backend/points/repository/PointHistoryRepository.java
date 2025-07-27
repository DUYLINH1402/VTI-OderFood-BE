package com.foodorder.backend.points.repository;

import com.foodorder.backend.points.entity.PointHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    Page<PointHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    List<PointHistory> findByUserIdOrderByCreatedAtAsc(Long userId);
    Page<PointHistory> findByUserIdOrderByCreatedAtAsc(Long userId, Pageable pageable);
}
