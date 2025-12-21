package com.foodorder.backend.food;

import com.foodorder.backend.food.dto.request.FoodFilterRequest;
import com.foodorder.backend.food.dto.request.FoodRequest;
import com.foodorder.backend.food.dto.request.FoodStatusUpdateRequest;
import com.foodorder.backend.food.dto.response.FoodResponse;
import com.foodorder.backend.food.service.FoodService;
import com.foodorder.backend.security.annotation.RequireAdmin;
import com.foodorder.backend.security.annotation.RequireStaff;
import com.foodorder.backend.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/foods")
@CrossOrigin("*")
public class FoodController {

    @Autowired
    private FoodService foodService;
    @Autowired
    private S3Service s3Service;

    // ==================== PUBLIC APIs (Không cần đăng nhập) ====================

    // LẤY DANH SÁCH MÓN MỚI
    @GetMapping("/new")
    public ResponseEntity<Page<FoodResponse>> getNewFoods(@PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(foodService.getNewFoods(pageable));
    }

    // LẤY DANH SÁCH MÓN NGON
    @GetMapping("/featured")
    public ResponseEntity<Page<FoodResponse>> getFeaturedFoods(@PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(foodService.getFeaturedFoods(pageable));
    }

    // LẤY DANH SÁCH MÓN ĐƯỢC ƯA THÍCH
    @GetMapping("/bestsellers")
    public ResponseEntity<Page<FoodResponse>> getBestSellerFoods(@PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(foodService.getBestSellerFoods(pageable));
    }

    // Lấy danh sách món ăn theo danh mục
    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<Page<FoodResponse>> getFoodsByCategoryId(
            @PathVariable Long categoryId,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(foodService.getFoodsByCategoryId(categoryId, pageable));
    }

    // Lấy danh sách món ăn theo danh mục bằng SLUG
    @GetMapping("/by-category-slug/{slug}")
    public ResponseEntity<Page<FoodResponse>> getFoodsByCategorySlug(
            @PathVariable String slug,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(foodService.getFoodsByCategorySlug(slug, pageable));
    }

    // Chi tiết món ăn theo SLUG
    @GetMapping("/slug/{slug}")
    public ResponseEntity<FoodResponse> getFoodBySlug(@PathVariable String slug) {
        FoodResponse foodResponse = foodService.getFoodBySlug(slug);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(foodResponse);
    }

    // Lấy chi tiết món ăn theo ID (Public)
    @GetMapping("/{id}")
    public ResponseEntity<FoodResponse> getFoodById(@PathVariable Long id) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(foodService.getFoodById(id));
    }

    // Lấy tất cả món ăn (Public - cho FE hiển thị)
    @GetMapping
    public ResponseEntity<Page<FoodResponse>> getAllFoods(@PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(foodService.getAllFoods(pageable));
    }

    // ==================== STAFF APIs (Staff và Admin) ====================

    /**
     * API lấy danh sách món ăn cho Staff quản lý
     * Hỗ trợ phân trang và bộ lọc theo tên, trạng thái, danh mục
     */
    @GetMapping("/management")
    @RequireStaff
    public ResponseEntity<Page<FoodResponse>> getFoodsForManagement(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(size = 20) Pageable pageable) {

        FoodFilterRequest filterRequest = FoodFilterRequest.builder()
                .name(name)
                .status(status)
                .categoryId(categoryId)
                .isActive(isActive)
                .build();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(foodService.getFoodsWithFilter(filterRequest, pageable));
    }

    /**
     * API cập nhật trạng thái món ăn (dành cho Staff)
     * Cho phép thay đổi status (AVAILABLE/UNAVAILABLE) hoặc isActive
     */
    @PatchMapping("/{id}/status")
    @RequireStaff
    public ResponseEntity<FoodResponse> updateFoodStatus(
            @PathVariable Long id,
            @RequestBody FoodStatusUpdateRequest request) {
        FoodResponse response = foodService.updateFoodStatus(id, request);
        return ResponseEntity.ok(response);
    }

    // ==================== ADMIN APIs (Chỉ Admin) ====================

    // TẠO MÓN ĂN MỚI - Chỉ Admin
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireAdmin
    public ResponseEntity<FoodResponse> createFood(@ModelAttribute FoodRequest foodRequest) {
        FoodResponse response = foodService.createFood(foodRequest);
        return ResponseEntity.ok(response);
    }

    // UPDATE MÓN ĂN - Chỉ Admin
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequireAdmin
    public ResponseEntity<FoodResponse> updateFood(@PathVariable Long id, @ModelAttribute FoodRequest foodRequest) {
        FoodResponse response = foodService.updateFood(id, foodRequest);
        return ResponseEntity.ok(response);
    }

    // XÓA MÓN ĂN - Chỉ Admin
    @DeleteMapping("/{id}")
    @RequireAdmin
    public ResponseEntity<Void> deleteFood(@PathVariable Long id) {
        foodService.deleteFood(id);
        return ResponseEntity.noContent().build();
    }

    // Upload ảnh món ăn - Chỉ Admin
    @PostMapping("/upload")
    @RequireAdmin
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = s3Service.uploadFile(file);
            return ResponseEntity.ok(imageUrl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }
}
