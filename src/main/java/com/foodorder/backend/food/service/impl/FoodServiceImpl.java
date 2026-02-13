package com.foodorder.backend.food.service.impl;

import com.foodorder.backend.food.dto.request.FoodFilterRequest;
import com.foodorder.backend.food.dto.request.FoodRequest;
import com.foodorder.backend.food.dto.request.FoodStatusUpdateRequest;
import com.foodorder.backend.food.dto.response.FoodResponse;
import com.foodorder.backend.food.dto.response.FoodVariantResponse;
import com.foodorder.backend.category.entity.Category;
import com.foodorder.backend.food.entity.Food;
import com.foodorder.backend.food.entity.FoodImage;
import com.foodorder.backend.exception.ForbiddenException;
import com.foodorder.backend.exception.ResourceNotFoundException;
import com.foodorder.backend.exception.BadRequestException;
import com.foodorder.backend.category.repository.CategoryRepository;
import com.foodorder.backend.food.repository.FoodImageRepository;
import com.foodorder.backend.food.repository.FoodRepository;
import com.foodorder.backend.food.repository.FoodVariantRepository;
import com.foodorder.backend.food.service.FoodService;
import com.foodorder.backend.security.CustomUserDetails;
import com.foodorder.backend.service.S3Service;
import com.foodorder.backend.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import com.foodorder.backend.food.entity.FoodStatus;

@Service
@Slf4j
public class FoodServiceImpl implements FoodService {

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private S3Service s3Service;

    @Autowired
    private FoodImageRepository foodImageRepository;

    @Autowired
    private FoodVariantRepository foodVariantRepository;

    // ==================== Helper Methods ====================

    /**
     * Lấy thông tin user hiện tại từ SecurityContext
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return userDetails.getUser();
        }
        return null;
    }

    /**
     * Kiểm tra user hiện tại có phải là SUPER_ADMIN không
     */
    private boolean isCurrentUserSuperAdmin() {
        User currentUser = getCurrentUser();
        return currentUser != null && currentUser.isSuperAdmin();
    }

    /**
     * Kiểm tra quyền thao tác trên dữ liệu được bảo vệ
     * Nếu dữ liệu được bảo vệ (isProtected = true) và user không phải SUPER_ADMIN, throw ForbiddenException
     */
    private void checkProtectedDataPermission(Boolean isProtected, String action) {
        if (Boolean.TRUE.equals(isProtected) && !isCurrentUserSuperAdmin()) {
            log.warn("User không có quyền {} dữ liệu được bảo vệ", action);
            throw new ForbiddenException(
                    "Dữ liệu được bảo vệ, chỉ Super Admin mới có quyền " + action,
                    "PROTECTED_DATA_ACCESS_DENIED"
            );
        }
    }

    private FoodResponse mapToDto(Food food) {
        FoodResponse response = modelMapper.map(food, FoodResponse.class);

        // Set category name
        if (food.getCategory() != null) {
            response.setCategoryName(food.getCategory().getName());
        }

        // Map status enum sang String
        if (food.getStatus() != null) {
            response.setStatus(food.getStatus().name());
        }

        // Map statusNote
        response.setStatusNote(food.getStatusNote());

        // Map totalLikes và totalShares (đảm bảo không null)
        response.setTotalLikes(food.getTotalLikes() != null ? food.getTotalLikes() : 0);
        response.setTotalShares(food.getTotalShares() != null ? food.getTotalShares() : 0);

        // Map isProtected
        response.setIsProtected(food.getIsProtected() != null ? food.getIsProtected() : false);

        return response;
    }

    // TẠO MÓN ĂN MỚI
    @Override
    public FoodResponse createFood(FoodRequest foodRequest) {

        // Tìm Category tương ứng
        Category category = categoryRepository.findById(foodRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("CATEGORY_NOT_FOUND"));

        // Map dữ liệu từ FoodRequest sang entity Food
        Food food = new Food();
        food.setName(foodRequest.getName());
        food.setDescription(foodRequest.getDescription());
        food.setPrice(BigDecimal.valueOf(foodRequest.getPrice()));
        food.setCategory(category);
        food.setCategory(category);
        // Kiểm tra ảnh và upload lên S3
        MultipartFile image = foodRequest.getImageUrl();
        if (image != null && !image.isEmpty()) {
            try {
                String imageUrl = s3Service.uploadFile(image);
                food.setImageUrl(imageUrl);
            } catch (IOException e) {
                throw new BadRequestException("IMAGE_UPLOAD_FAILED", "IMAGE_UPLOAD_FAILED");
            }
        }

        // Lưu vào database
        Food savedFood = foodRepository.save(food);

        // Map sang DTO để trả về response
        return modelMapper.map(savedFood, FoodResponse.class);
    }

    // UPDATE MÓN ĂN
    // Nếu món ăn được bảo vệ (isProtected = true), chỉ SUPER_ADMIN mới có quyền cập nhật
    @Override
    public FoodResponse updateFood(Long id, FoodRequest foodRequest) {

        // Tìm Food hiện tại
        Food existingFood = foodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FOOD_NOT_FOUND"));

        // Kiểm tra quyền nếu dữ liệu được bảo vệ
        checkProtectedDataPermission(existingFood.getIsProtected(), "cập nhật");

        // Tìm Category mới
        Category category = categoryRepository.findById(foodRequest.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("CATEGORY_NOT_FOUND"));

        // Cập nhật dữ liệu
        existingFood.setName(foodRequest.getName());
        existingFood.setDescription(foodRequest.getDescription());
        existingFood.setPrice(BigDecimal.valueOf(foodRequest.getPrice()));
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
                throw new BadRequestException("IMAGE_UPLOAD_FAILED", "IMAGE_UPLOAD_FAILED");
            }
        }


        // Lưu cập nhật
        Food updatedFood = foodRepository.save(existingFood);

        // Map sang DTO
        return modelMapper.map(updatedFood, FoodResponse.class);
    }


    // Nếu món ăn được bảo vệ (isProtected = true), chỉ SUPER_ADMIN mới có quyền xóa
    @Override
    public void deleteFood(Long id) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FOOD_NOT_FOUND: " + id));

        // Kiểm tra quyền nếu dữ liệu được bảo vệ
        checkProtectedDataPermission(food.getIsProtected(), "xóa");

        foodRepository.deleteById(id);
    }

    @Override
    public FoodResponse getFoodById(Long id) {
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FOOD_NOT_FOUND: " + id));
        return mapToDto(food);
    }

    @Override
    public Page<FoodResponse> getAllFoods( Pageable pageable) {
        return foodRepository.findAll(pageable).map(this::mapToDto);
    }

    @Override
    public Page<FoodResponse> getNewFoods(Pageable pageable) {
        Page<Food> foods = foodRepository.findByIsNewTrue(pageable);
        return foods.map(this::mapToDto);
    }

    @Override
    public Page<FoodResponse> getFeaturedFoods(Pageable pageable) {
        Page<Food> featuredFoods = foodRepository.findByIsFeaturedTrue(pageable);
        return featuredFoods.map(this::mapToDto);
    }

    @Override
    public Page<FoodResponse> getBestSellerFoods(Pageable pageable) {
        Page<Food> bestSellers = foodRepository.findByIsBestSellerTrue(pageable);
        return bestSellers.map(this::mapToDto);
    }

    @Override
    public Page<FoodResponse> getFoodsByCategoryId(Long categoryId, Pageable pageable) {
        // Kiểm tra xem categoryId có tồn tại không
        if (!categoryRepository.existsById(categoryId)) {
            throw new ResourceNotFoundException("CATEGORY_NOT_FOUND: " + categoryId);
        }

        // Lấy danh sách món ăn theo categoryId
        Page<Food> foods = foodRepository.findByCategoryId(categoryId, pageable);

        // Map từ Page<Food> sang Page<FoodResponse>
        return foods.map(this::mapToDto);
    }

    @Override
    public Page<FoodResponse> getFoodsByCategorySlug(String slug, Pageable pageable) {
        // Kiểm tra xem categorySlug có tồn tại không
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("CATEGORY_NOT_FOUND_BY_SLUG: " + slug));

        // Lấy danh sách món ăn theo categoryId của danh mục vừa tìm được
        Page<Food> foods = foodRepository.findByCategoryId(category.getId(), pageable);

        // Map từng đối tượng Food sang FoodResponse (DTO trả về cho FE)
        return foods.map(this::mapToDto);
    }

    @Override
    public FoodResponse getFoodBySlug(String slug) {
        Food food = foodRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("FOOD_NOT_FOUND_BY_SLUG: " + slug));

        // Ánh xạ dữ liệu cơ bản
        FoodResponse response = modelMapper.map(food, FoodResponse.class);

        // Map status enum sang String đúng cách
        if (food.getStatus() != null) {
            response.setStatus(food.getStatus().name());
        }

        // Map statusNote
        response.setStatusNote(food.getStatusNote());

        // Lấy ảnh phụ (nếu có)
        List<String> imageUrls = foodImageRepository.findByFoodIdOrderByDisplayOrderAsc(food.getId())
                .stream()
                .map(FoodImage::getImageUrl)
                .collect(Collectors.toList());
        response.setImages(imageUrls);

        // Lấy danh sách biến thể (nếu có)
        List<FoodVariantResponse> variants = foodVariantRepository.findByFoodId(food.getId())
                .stream()
                .map(variant -> {
                    FoodVariantResponse v = new FoodVariantResponse();
                    v.setId(variant.getId());
                    v.setName(variant.getName());
                    v.setExtraPrice(variant.getExtraPrice());
                    v.setDefault(Boolean.TRUE.equals(variant.getIsDefault()));
                    return v;
                })
                .collect(Collectors.toList());
        response.setVariants(variants);

        return response;
    }

    /**
     * Lấy danh sách món ăn với bộ lọc cho Staff quản lý
     * Hỗ trợ lọc theo tên, trạng thái, danh mục, trạng thái hoạt động
     */
    @Override
    public Page<FoodResponse> getFoodsWithFilter(FoodFilterRequest filterRequest, Pageable pageable) {
        // Chuyển đổi status từ String sang FoodStatus enum (nếu có)
        FoodStatus foodStatus = null;
        if (filterRequest.getStatus() != null && !filterRequest.getStatus().isEmpty()) {
            try {
                foodStatus = FoodStatus.valueOf(filterRequest.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Nếu status không hợp lệ, bỏ qua và không lọc theo status
                foodStatus = null;
            }
        }

        // Gọi repository với các tham số lọc
        Page<Food> foods = foodRepository.findWithFilter(
                filterRequest.getName(),
                foodStatus,
                filterRequest.getCategoryId(),
                filterRequest.getIsActive(),
                pageable
        );

        // Map từ Page<Food> sang Page<FoodResponse>
        return foods.map(this::mapToDto);
    }

    /**
     * Cập nhật trạng thái món ăn (dành cho Staff)
     * Cho phép thay đổi status (AVAILABLE/UNAVAILABLE) hoặc isActive
     * Nếu món ăn được bảo vệ (isProtected = true), chỉ SUPER_ADMIN mới có quyền cập nhật
     */
    @Override
    public FoodResponse updateFoodStatus(Long id, FoodStatusUpdateRequest request) {
        // Tìm món ăn theo ID
        Food food = foodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FOOD_NOT_FOUND"));

        // Kiểm tra quyền nếu dữ liệu được bảo vệ
        checkProtectedDataPermission(food.getIsProtected(), "cập nhật trạng thái");

        // Cập nhật status nếu có
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            try {
                FoodStatus newStatus = FoodStatus.valueOf(request.getStatus().toUpperCase());
                food.setStatus(newStatus);
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("INVALID_STATUS", "Trạng thái không hợp lệ. Chỉ chấp nhận: AVAILABLE, UNAVAILABLE");
            }
        }

        // Cập nhật isActive nếu có
        if (request.getIsActive() != null) {
            food.setIsActive(request.getIsActive());
        }

        // Cập nhật ghi chú trạng thái nếu có
        if (request.getStatusNote() != null) {
            food.setStatusNote(request.getStatusNote());
        }

        // Lưu thay đổi
        Food updatedFood = foodRepository.save(food);

        // Trả về response
        return mapToDto(updatedFood);
    }

}
