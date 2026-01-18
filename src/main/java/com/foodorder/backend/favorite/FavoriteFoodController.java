package com.foodorder.backend.favorite;

import com.foodorder.backend.exception.ResourceNotFoundException;
import com.foodorder.backend.favorite.dto.request.FavoriteRequest;
import com.foodorder.backend.favorite.dto.response.FavoriteFoodResponse;
import com.foodorder.backend.favorite.service.FavoriteFoodService;
import com.foodorder.backend.order.exception.UnauthorizedException;
import com.foodorder.backend.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller quản lý danh sách món ăn yêu thích của người dùng
 */
@RestController
@RequestMapping("/api/favorites")
@Tag(name = "Favorites", description = "API quản lý danh sách món ăn yêu thích - Yêu cầu đăng nhập")
public class FavoriteFoodController {

    private final FavoriteFoodService favoriteFoodService;

    @Autowired
    public FavoriteFoodController(FavoriteFoodService favoriteFoodService) {
        this.favoriteFoodService = favoriteFoodService;
    }

    @Operation(summary = "Lấy danh sách yêu thích", description = "Lấy danh sách các món ăn yêu thích của người dùng đang đăng nhập.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @GetMapping
    public ResponseEntity<List<FavoriteFoodResponse>> getFavorites(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        // Kiểm tra xác thực người dùng
        if (userDetails == null) {
            throw new ResourceNotFoundException("USER_NOT_AUTHENTICATED");
        }

        Long userId = userDetails.getId();
        List<FavoriteFoodResponse> favorites = favoriteFoodService.getFavorites(userId);
        return ResponseEntity.ok(favorites);
    }

    @Operation(summary = "Thêm vào yêu thích", description = "Thêm một món ăn vào danh sách yêu thích.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thêm thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy món ăn")
    })
    @PostMapping
    public ResponseEntity<?> addFavorite(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody FavoriteRequest request) {
        // Kiểm tra xác thực người dùng
        if (userDetails == null) {
            throw new ResourceNotFoundException("USER_NOT_AUTHENTICATED");
        }

        Long userId = userDetails.getId();
        favoriteFoodService.addToFavorites(userId, request);
        return ResponseEntity.ok("Added to favorites!");
    }

    @Operation(summary = "Xóa khỏi yêu thích", description = "Xóa một món ăn khỏi danh sách yêu thích.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    @DeleteMapping
    public ResponseEntity<?> removeFavorite(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody FavoriteRequest request) {
        // Kiểm tra xác thực người dùng
        if (userDetails == null) {
            throw new ResourceNotFoundException("USER_NOT_AUTHENTICATED");
        }

        Long userId = userDetails.getId();
        favoriteFoodService.removeFromFavorites(userId, request);
        return ResponseEntity.ok("Removed from favorites!");
    }
}
