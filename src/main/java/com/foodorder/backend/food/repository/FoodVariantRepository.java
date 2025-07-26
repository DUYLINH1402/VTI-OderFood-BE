package com.foodorder.backend.food.repository;

import com.foodorder.backend.food.entity.FoodVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodVariantRepository extends JpaRepository<FoodVariant, Long> {
    List<FoodVariant> findByFoodId(Long foodId);
}

