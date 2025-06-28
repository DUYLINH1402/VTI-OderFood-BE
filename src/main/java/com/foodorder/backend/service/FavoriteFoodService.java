package com.foodorder.backend.service;

import com.foodorder.backend.dto.request.FavoriteRequest;
import com.foodorder.backend.dto.response.FavoriteFoodResponse;

import java.util.List;

public interface FavoriteFoodService {
    void addToFavorites(Long userId, FavoriteRequest request);
    void removeFromFavorites(Long userId, FavoriteRequest request);
    List<FavoriteFoodResponse> getFavorites(Long userId);
}

