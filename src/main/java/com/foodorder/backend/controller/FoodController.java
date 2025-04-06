package com.foodorder.backend.controller;

import com.foodorder.backend.dto.request.FoodRequest;
import com.foodorder.backend.dto.response.FoodResponse;
import com.foodorder.backend.service.FoodService;
import com.foodorder.backend.service.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ResponseEntity<List<FoodResponse>> getAllFoods() {
        return ResponseEntity.ok(foodService.getAllFoods());
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
