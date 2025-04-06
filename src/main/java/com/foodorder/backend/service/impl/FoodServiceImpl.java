package com.foodorder.backend.service.impl;

import com.foodorder.backend.dto.request.FoodRequest;
import com.foodorder.backend.dto.response.FoodResponse;
import com.foodorder.backend.entity.Category;
import com.foodorder.backend.entity.Food;
import com.foodorder.backend.exception.ResourceNotFoundException;
import com.foodorder.backend.repository.CategoryRepository;
import com.foodorder.backend.repository.FoodRepository;
import com.foodorder.backend.service.FoodService;
import com.foodorder.backend.service.S3Service;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FoodServiceImpl implements FoodService {

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private S3Service s3Service;


    private FoodResponse mapToDto(Food food) {
        FoodResponse response = modelMapper.map(food, FoodResponse.class);
        response.setCategoryName(food.getCategory().getName());
        return response;
    }

    // TẠO MÓN ĂN MỚI
    @Override
    public FoodResponse createFood(FoodRequest foodRequest) {

        // Tìm Category tương ứng
        Category category = categoryRepository.findById(foodRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // Map dữ liệu từ FoodRequest sang entity Food
        Food food = new Food();
        food.setName(foodRequest.getName());
        food.setDescription(foodRequest.getDescription());
        food.setPrice(foodRequest.getPrice());
        food.setCategory(category);
        food.setCategory(category);
        // Kiểm tra ảnh và upload lên S3
        MultipartFile image = foodRequest.getImageUrl();
        if (image != null && !image.isEmpty()) {
            try {
                String imageUrl = s3Service.uploadFile(image);
                food.setImageUrl(imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("Upload image failed", e);
            }
        }

        // Lưu vào database
        Food savedFood = foodRepository.save(food);

        // Map sang DTO để trả về response
        return modelMapper.map(savedFood, FoodResponse.class);
    }

    // UPDATE MÓN ĂN
    @Override
    public FoodResponse updateFood(Long id, FoodRequest foodRequest) {

        // Tìm Food hiện tại
        Food existingFood = foodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food not found"));

        // Tìm Category mới
        Category category = categoryRepository.findById(foodRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // Cập nhật dữ liệu
        existingFood.setName(foodRequest.getName());
        existingFood.setDescription(foodRequest.getDescription());
        existingFood.setPrice(foodRequest.getPrice());
        existingFood.setCategory(category);

        // Kiểm tra nếu có upload ảnh mới
        MultipartFile image = foodRequest.getImageUrl();
        if (image != null && !image.isEmpty()) {
            try {
                //  Nếu đã có ảnh cũ → xóa khỏi S3
                String oldImageUrl = existingFood.getImageUrl();
                if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                    s3Service.deleteFile(oldImageUrl); // tạo hàm này ở dưới
                }

                //  Upload ảnh mới
                String imageUrl = s3Service.uploadFile(image);
                existingFood.setImageUrl(imageUrl);

            } catch (IOException e) {
                throw new RuntimeException("Image upload failed", e);
            }
        }


        // Lưu cập nhật
        Food updatedFood = foodRepository.save(existingFood);

        // Map sang DTO
        return modelMapper.map(updatedFood, FoodResponse.class);
    }


    @Override
    public void deleteFood(Long id) {
        if (!foodRepository.existsById(id)) {
            throw new ResourceNotFoundException("Food not found with id: " + id);
        }
        foodRepository.deleteById(id);
    }

    @Override
    public FoodResponse getFoodById(Long id) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Food not found with id: " + id));
        return mapToDto(food);
    }

    @Override
    public List<FoodResponse> getAllFoods() {
        return foodRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
}
