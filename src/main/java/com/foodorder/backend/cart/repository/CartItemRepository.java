package com.foodorder.backend.cart.repository;

import com.foodorder.backend.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserId(Long userId);
    Optional<CartItem> findByUserIdAndFoodId(Long userId, Long foodId);
    void deleteByUserId(Long userId);
    void deleteByUserIdAndFoodId(Long userId, Long foodId);
    Optional<CartItem> findByUserIdAndFoodIdAndVariantId(Long userId, Long foodId, Long variantId);

}

