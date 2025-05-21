package com.foodorder.backend.controller;

import com.foodorder.backend.dto.request.FoodRequest;
import com.foodorder.backend.dto.response.FoodResponse;
import com.foodorder.backend.service.FoodService;
import com.foodorder.backend.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequestMapping("/api/foods")
@CrossOrigin("*")
public class FoodController {


    @Autowired
    private FoodService foodService;
    @Autowired
    private S3Service s3Service;

    // LẤY DANH SÁCH MÓN MỚI
    @GetMapping("/new")
    public ResponseEntity<Page<FoodResponse>> getNewFoods(@PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(foodService.getNewFoods(pageable));
    }

    // LẤY DANH SÁCH MÓN NGON
    @GetMapping("/featured")
    public ResponseEntity<Page<FoodResponse>> getFeaturedFoods(@PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(foodService.getFeaturedFoods(pageable));
    }

    // LẤY DANH SÁCH MÓN ĐƯỢC ƯA THÍCH
    @GetMapping("/bestsellers")
    public ResponseEntity<Page<FoodResponse>> getBestSellerFoods(@PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(foodService.getBestSellerFoods(pageable));
    }
    // Lấy danh sách món ăn theo danh mục
    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<Page<FoodResponse>> getFoodsByCategoryId(@PathVariable Long categoryId, @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(foodService.getFoodsByCategoryId(categoryId, pageable));
    }
    // Lấy danh sách món ăn theo danh mục bằng SLUG
    @GetMapping("/by-category-slug/{slug}")
    public ResponseEntity<Page<FoodResponse>> getFoodsByCategorySlug(
            @PathVariable String slug,
            @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(foodService.getFoodsByCategorySlug(slug, pageable));
    }

    // Chi tiết món ăn theo SLUG
    @GetMapping("/slug/{slug}")
    public ResponseEntity<FoodResponse> getFoodBySlug(@PathVariable String slug) {
        FoodResponse foodResponse = foodService.getFoodBySlug(slug);
        return ResponseEntity.ok(foodResponse);
    }


    // TẠO MÓN ĂN MỚI
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FoodResponse> createFood(@ModelAttribute FoodRequest foodRequest) {
        FoodResponse response = foodService.createFood(foodRequest);
        return ResponseEntity.ok(response);
    }

    // UPDATE MÓN ĂN
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FoodResponse> updateFood(@PathVariable Long id, @ModelAttribute FoodRequest foodRequest) {
        FoodResponse response = foodService.updateFood(id, foodRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFood(@PathVariable Long id) {
        foodService.deleteFood(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<FoodResponse> getFoodById(@PathVariable Long id) {
        return ResponseEntity.ok(foodService.getFoodById(id));
    }

    @GetMapping
    public ResponseEntity<Page<FoodResponse>> getAllFoods(@PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok(foodService.getAllFoods(pageable));
    }

//    Upload ảnh món ăn
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = s3Service.uploadFile(file);
            return ResponseEntity.ok(imageUrl);
        } catch (Exception e) {
//            e.printStackTrace(); // log chi tiết ra console
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }

}

