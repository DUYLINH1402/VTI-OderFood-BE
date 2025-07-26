package com.foodorder.backend.food.repository;

import com.foodorder.backend.food.entity.FoodImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodImageRepository extends JpaRepository<FoodImage, Long> {
    List<FoodImage> findByFoodIdOrderByDisplayOrderAsc(Long foodId);
}

