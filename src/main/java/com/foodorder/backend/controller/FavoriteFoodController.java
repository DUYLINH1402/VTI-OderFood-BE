package com.foodorder.backend.controller;

import com.foodorder.backend.dto.request.FavoriteRequest;
import com.foodorder.backend.dto.response.FavoriteFoodResponse;
import com.foodorder.backend.security.CustomUserDetails;
import com.foodorder.backend.service.FavoriteFoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/favorites")
public class FavoriteFoodController {

    private final FavoriteFoodService favoriteFoodService;

    @Autowired
    public FavoriteFoodController(FavoriteFoodService favoriteFoodService) {
        this.favoriteFoodService = favoriteFoodService;
    }

    @GetMapping
    public ResponseEntity<List<FavoriteFoodResponse>> getFavorites(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getId();
        List<FavoriteFoodResponse> favorites = favoriteFoodService.getFavorites(userId);
        return ResponseEntity.ok(favorites);
    }

    @PostMapping
    public ResponseEntity<?> addFavorite(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody FavoriteRequest request) {
        Long userId = userDetails.getId();
        favoriteFoodService.addToFavorites(userId, request);
        return ResponseEntity.ok("Added to favorites!");
    }

    @DeleteMapping
    public ResponseEntity<?> removeFavorite(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody FavoriteRequest request) {
        Long userId = userDetails.getId();
        favoriteFoodService.removeFromFavorites(userId, request);
        return ResponseEntity.ok("Removed from favorites!");
    }
}
