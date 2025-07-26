package com.foodorder.backend.favorite.service.impl;

import com.foodorder.backend.favorite.dto.request.FavoriteRequest;
import com.foodorder.backend.favorite.dto.response.FavoriteFoodResponse;
import com.foodorder.backend.favorite.entity.FavoriteFood;
import com.foodorder.backend.food.entity.Food;
import com.foodorder.backend.food.entity.FoodVariant;
import com.foodorder.backend.user.entity.User;
import com.foodorder.backend.exception.ResourceNotFoundException;
import com.foodorder.backend.favorite.repository.FavoriteFoodRepository;
import com.foodorder.backend.food.repository.FoodRepository;
import com.foodorder.backend.food.repository.FoodVariantRepository;
import com.foodorder.backend.user.repository.UserRepository;
import com.foodorder.backend.favorite.service.FavoriteFoodService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FavoriteFoodServiceImpl implements FavoriteFoodService {

    private final FavoriteFoodRepository favoriteRepo;
    private final UserRepository userRepo;
    private final FoodRepository foodRepo;
    private final FoodVariantRepository variantRepo;

    @Override
    public void addToFavorites(Long userId, FavoriteRequest request) {
        Long foodId = request.getFoodId();
        Long variantId = request.getVariantId();

        boolean exists = favoriteRepo.findByUserIdAndFoodIdAndVariantId(userId, foodId, variantId).isPresent();
        if (exists) return;

        User user = userRepo.findById(userId).orElseThrow(() -> new ResourceNotFoundException("USER_NOT_FOUND"));
        Food food = foodRepo.findById(foodId).orElseThrow(() -> new ResourceNotFoundException("FOOD_NOT_FOUND"));

        FoodVariant variant = null;
        if (variantId != null) {
            variant = variantRepo.findById(variantId).orElseThrow(() -> new ResourceNotFoundException("VARIANT_NOT_FOUND"));
        }

        FavoriteFood favorite = new FavoriteFood();
        favorite.setUser(user);
        favorite.setFood(food);
        favorite.setVariant(variant);
        favorite.setCreatedAt(LocalDateTime.now());

        favoriteRepo.save(favorite);
    }

    @Override
    public void removeFromFavorites(Long userId, FavoriteRequest request) {
        favoriteRepo.deleteByUserIdAndFoodIdAndVariantId(userId, request.getFoodId(), request.getVariantId());
    }

    @Override
    public List<FavoriteFoodResponse> getFavorites(Long userId) {
        return favoriteRepo.findByUserId(userId).stream()
                .map(FavoriteFoodResponse::fromEntity)
                .collect(Collectors.toList());
    }
}

