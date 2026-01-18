package com.foodorder.backend.food;

import com.foodorder.backend.food.dto.request.FoodFilterRequest;
import com.foodorder.backend.food.dto.request.FoodStatusUpdateRequest;
import com.foodorder.backend.food.dto.response.FoodResponse;
import com.foodorder.backend.food.service.FoodService;
import com.foodorder.backend.security.annotation.RequireStaff;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * Controller quản lý món ăn - API Public và Staff
 * Các API Admin đã được tách sang FoodAdminController
 */
@RestController
@RequestMapping("/api/foods")
@CrossOrigin("*")
@Tag(name = "Foods", description = "API quản lý món ăn - Public và Staff")
public class FoodController {

    @Autowired
    private FoodService foodService;

    // ==================== PUBLIC APIs (Không cần đăng nhập) ====================

    @Operation(summary = "Lấy danh sách món mới", description = "Lấy danh sách các món ăn mới nhất, sắp xếp theo thời gian tạo.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/new")
    public ResponseEntity<Page<FoodResponse>> getNewFoods(
            @Parameter(description = "Thông tin phân trang") @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(foodService.getNewFoods(pageable));
    }

    @Operation(summary = "Lấy danh sách món nổi bật", description = "Lấy danh sách các món ăn được đánh dấu là nổi bật.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/featured")
    public ResponseEntity<Page<FoodResponse>> getFeaturedFoods(
            @Parameter(description = "Thông tin phân trang") @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(foodService.getFeaturedFoods(pageable));
    }

    @Operation(summary = "Lấy danh sách món bán chạy", description = "Lấy danh sách các món ăn bán chạy nhất dựa trên số lượng đã bán.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping("/bestsellers")
    public ResponseEntity<Page<FoodResponse>> getBestSellerFoods(
            @Parameter(description = "Thông tin phân trang") @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(foodService.getBestSellerFoods(pageable));
    }

    @Operation(summary = "Lấy món ăn theo danh mục (ID)", description = "Lấy danh sách món ăn thuộc một danh mục cụ thể theo ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy danh mục")
    })
    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<Page<FoodResponse>> getFoodsByCategoryId(
            @Parameter(description = "ID của danh mục") @PathVariable Long categoryId,
            @Parameter(description = "Thông tin phân trang") @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(foodService.getFoodsByCategoryId(categoryId, pageable));
    }

    @Operation(summary = "Lấy món ăn theo danh mục (Slug)", description = "Lấy danh sách món ăn thuộc một danh mục cụ thể theo slug.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy danh mục")
    })
    @GetMapping("/by-category-slug/{slug}")
    public ResponseEntity<Page<FoodResponse>> getFoodsByCategorySlug(
            @Parameter(description = "Slug của danh mục") @PathVariable String slug,
            @Parameter(description = "Thông tin phân trang") @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(foodService.getFoodsByCategorySlug(slug, pageable));
    }

    @Operation(summary = "Chi tiết món ăn (Slug)", description = "Lấy thông tin chi tiết của một món ăn theo slug.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = FoodResponse.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy món ăn")
    })
    @GetMapping("/slug/{slug}")
    public ResponseEntity<FoodResponse> getFoodBySlug(
            @Parameter(description = "Slug của món ăn") @PathVariable String slug) {
        FoodResponse foodResponse = foodService.getFoodBySlug(slug);
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(foodResponse);
    }

    @Operation(summary = "Chi tiết món ăn (ID)", description = "Lấy thông tin chi tiết của một món ăn theo ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công",
                    content = @Content(schema = @Schema(implementation = FoodResponse.class))),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy món ăn")
    })
    @GetMapping("/{id}")
    public ResponseEntity<FoodResponse> getFoodById(
            @Parameter(description = "ID của món ăn") @PathVariable Long id) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(foodService.getFoodById(id));
    }

    @Operation(summary = "Lấy tất cả món ăn", description = "Lấy danh sách tất cả món ăn đang hoạt động, có phân trang.")
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping
    public ResponseEntity<Page<FoodResponse>> getAllFoods(
            @Parameter(description = "Thông tin phân trang") @PageableDefault(size = 12) Pageable pageable) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES).cachePublic())
                .body(foodService.getAllFoods(pageable));
    }

    // ==================== STAFF APIs (Staff và Admin) ====================

    /**
     * API lấy danh sách món ăn cho Staff quản lý
     * Hỗ trợ phân trang và bộ lọc theo tên, trạng thái, danh mục
     */
    @Operation(summary = "Quản lý món ăn (Staff)",
            description = "Lấy danh sách món ăn cho Staff quản lý. Hỗ trợ lọc theo tên, trạng thái, danh mục và trạng thái hoạt động.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập")
    })
    @GetMapping("/management")
    @RequireStaff
    public ResponseEntity<Page<FoodResponse>> getFoodsForManagement(
            @Parameter(description = "Tên món ăn (tìm kiếm)") @RequestParam(required = false) String name,
            @Parameter(description = "Trạng thái (AVAILABLE/UNAVAILABLE)") @RequestParam(required = false) String status,
            @Parameter(description = "ID danh mục") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Trạng thái hoạt động") @RequestParam(required = false) Boolean isActive,
            @Parameter(description = "Thông tin phân trang") @PageableDefault(size = 20) Pageable pageable) {

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
    @Operation(summary = "Cập nhật trạng thái món ăn (Staff)",
            description = "Cập nhật trạng thái món ăn. Cho phép thay đổi status (AVAILABLE/UNAVAILABLE) hoặc isActive.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công",
                    content = @Content(schema = @Schema(implementation = FoodResponse.class))),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy món ăn")
    })
    @PatchMapping("/{id}/status")
    @RequireStaff
    public ResponseEntity<FoodResponse> updateFoodStatus(
            @Parameter(description = "ID của món ăn") @PathVariable Long id,
            @RequestBody FoodStatusUpdateRequest request) {
        FoodResponse response = foodService.updateFoodStatus(id, request);
        return ResponseEntity.ok(response);
    }
}
