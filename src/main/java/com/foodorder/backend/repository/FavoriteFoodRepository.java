package com.foodorder.backend.repository;

import com.foodorder.backend.entity.FavoriteFood;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteFoodRepository extends JpaRepository<FavoriteFood, Long> {
    Optional<FavoriteFood> findByUserIdAndFoodIdAndVariantId(Long userId, Long foodId, Long variantId);
    List<FavoriteFood> findByUserId(Long userId);
    void deleteByUserIdAndFoodIdAndVariantId(Long userId, Long foodId, Long variantId);
}
