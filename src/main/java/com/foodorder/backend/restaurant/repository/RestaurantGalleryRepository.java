package com.foodorder.backend.restaurant.repository;

import com.foodorder.backend.restaurant.entity.RestaurantGallery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository để truy vấn gallery của nhà hàng
 */
@Repository
public interface RestaurantGalleryRepository extends JpaRepository<RestaurantGallery, Long> {

    /**
     * Lấy danh sách gallery theo restaurantInfoId, sắp xếp theo displayOrder
     */
    List<RestaurantGallery> findByRestaurantInfoIdOrderByDisplayOrderAsc(Long restaurantInfoId);
}

