package com.foodorder.backend.food.service;

import com.foodorder.backend.food.dto.request.FoodFilterRequest;
import com.foodorder.backend.food.dto.request.FoodRequest;
import com.foodorder.backend.food.dto.request.FoodStatusUpdateRequest;
import com.foodorder.backend.food.dto.response.FoodResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    /**
     * Lấy danh sách món ăn có bộ lọc cho Staff quản lý
     * Hỗ trợ lọc theo tên, trạng thái, danh mục, trạng thái hoạt động
     */
    Page<FoodResponse> getFoodsWithFilter(FoodFilterRequest filterRequest, Pageable pageable);

    /**
     * Cập nhật trạng thái món ăn (dành cho Staff)
     * Cho phép thay đổi status (AVAILABLE/UNAVAILABLE) hoặc isActive
     */
    FoodResponse updateFoodStatus(Long id, FoodStatusUpdateRequest request);
}