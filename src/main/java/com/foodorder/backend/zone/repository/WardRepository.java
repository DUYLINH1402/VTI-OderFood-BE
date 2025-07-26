package com.foodorder.backend.zone.repository;

import com.foodorder.backend.zone.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WardRepository extends JpaRepository<Ward, Long> {
    List<Ward> findByDistrictId(Long districtId);
}
