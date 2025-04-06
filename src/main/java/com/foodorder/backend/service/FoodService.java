package com.foodorder.backend.service;

import com.foodorder.backend.dto.request.FoodRequest;
import com.foodorder.backend.dto.response.FoodResponse;
import com.foodorder.backend.entity.Food;

import java.util.List;

public interface FoodService {
    FoodResponse createFood(FoodRequest request);
    FoodResponse updateFood(Long id, FoodRequest request);
    void deleteFood(Long id);
    FoodResponse getFoodById(Long id);
    List<FoodResponse> getAllFoods();
}