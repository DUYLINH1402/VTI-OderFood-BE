package com.foodorder.backend.service;

import com.foodorder.backend.dto.request.FoodRequest;
import com.foodorder.backend.dto.response.FoodResponse;
import com.foodorder.backend.entity.Food;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FoodService {
    FoodResponse createFood(FoodRequest request);
    FoodResponse updateFood(Long id, FoodRequest request);
    void deleteFood(Long id);
    FoodResponse getFoodById(Long id);
    Page<FoodResponse> getAllFoods(Pageable pageable);

    Page<FoodResponse> getNewFoods(Pageable pageable);

    Page<FoodResponse> getFeaturedFoods(Pageable pageable);

    Page<FoodResponse> getBestSellerFoods(Pageable pageable);

    Page<FoodResponse> getFoodsByCategoryId(Long categoryId, Pageable pageable);

    Page<FoodResponse> getFoodsByCategorySlug(String slug, Pageable pageable);

    FoodResponse getFoodBySlug(String slug);
}