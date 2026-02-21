package com.foodorder.backend.restaurant.repository;

import com.foodorder.backend.restaurant.entity.RestaurantInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository để truy vấn thông tin nhà hàng
 */
@Repository
public interface RestaurantInfoRepository extends JpaRepository<RestaurantInfo, Long> {
}

