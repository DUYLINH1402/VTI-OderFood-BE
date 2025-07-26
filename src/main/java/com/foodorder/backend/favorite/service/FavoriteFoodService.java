package com.foodorder.backend.favorite.service;

import com.foodorder.backend.favorite.dto.request.FavoriteRequest;
import com.foodorder.backend.favorite.dto.response.FavoriteFoodResponse;

import java.util.List;

public interface FavoriteFoodService {
    void addToFavorites(Long userId, FavoriteRequest request);
    void removeFromFavorites(Long userId, FavoriteRequest request);
    List<FavoriteFoodResponse> getFavorites(Long userId);
}

