package com.foodorder.backend.repository;

import com.foodorder.backend.entity.Food;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FoodRepository extends JpaRepository<Food, Long> {
    Page<Food> findByIsNewTrue(Pageable pageable);

    Page<Food> findByIsFeaturedTrue(Pageable pageable);

    Page<Food> findByIsBestSellerTrue(Pageable pageable);

    Page<Food> findByCategoryId(Long categoryId, Pageable pageable);

    Optional<Food> findBySlug(String slug);

}