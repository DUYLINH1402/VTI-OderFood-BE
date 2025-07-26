package com.foodorder.backend.zone.repository;

import com.foodorder.backend.zone.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DistrictRepository extends JpaRepository<District, Long> {
    // Nếu cần tìm kiếm nâng cao thì bổ sung sau
}
