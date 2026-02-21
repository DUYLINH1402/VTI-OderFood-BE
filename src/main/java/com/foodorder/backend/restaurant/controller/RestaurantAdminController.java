package com.foodorder.backend.restaurant.controller;

import com.foodorder.backend.restaurant.dto.GalleryRequest;
import com.foodorder.backend.restaurant.dto.RestaurantResponseDTO;
import com.foodorder.backend.restaurant.dto.RestaurantUpdateRequest;
import com.foodorder.backend.restaurant.service.RestaurantService;
import com.foodorder.backend.security.annotation.RequireAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xử lý API Admin cho quản lý thông tin nhà hàng
 * Yêu cầu quyền ADMIN
 */
@RestController
@RequestMapping("/api/admin/restaurant")
@RequiredArgsConstructor
@Tag(name = "Restaurant Admin", description = "API quản lý thông tin nhà hàng dành cho Admin")
public class RestaurantAdminController {

    private final RestaurantService restaurantService;

    // ==================== RESTAURANT INFO APIs ====================

    /**
     * Lấy thông tin nhà hàng (Admin)
     */
    @Operation(
            summary = "Lấy thông tin nhà hàng",
            description = "Lấy thông tin chi tiết nhà hàng để chỉnh sửa"
    )
    @ApiResponse(responseCode = "200", description = "Thành công")
    @GetMapping
    @RequireAdmin
    public ResponseEntity<RestaurantResponseDTO> getRestaurantInfo() {
        return ResponseEntity.ok(restaurantService.getRestaurantDetails());
    }

    /**
     * Cập nhật thông tin nhà hàng
     */
    @Operation(
            summary = "Cập nhật thông tin nhà hàng",
            description = "Cập nhật tên, địa chỉ, số điện thoại, mô tả, giờ mở cửa của nhà hàng"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cập nhật thành công",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RestaurantResponseDTO.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy nhà hàng")
    })
    @PutMapping
    @RequireAdmin
    public ResponseEntity<RestaurantResponseDTO> updateRestaurantInfo(
            @Valid @RequestBody RestaurantUpdateRequest request) {
        return ResponseEntity.ok(restaurantService.updateRestaurantInfo(request));
    }

    // ==================== GALLERY APIs ====================

    /**
     * Thêm hình ảnh vào gallery
     */
    @Operation(
            summary = "Thêm hình ảnh gallery",
            description = "Thêm một hình ảnh mới vào gallery nhà hàng"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Thêm thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ")
    })
    @PostMapping("/gallery")
    @RequireAdmin
    public ResponseEntity<RestaurantResponseDTO> addGalleryImage(
            @Valid @RequestBody GalleryRequest request) {
        return ResponseEntity.ok(restaurantService.addGalleryImage(request));
    }

    /**
     * Cập nhật hình ảnh gallery
     */
    @Operation(
            summary = "Cập nhật hình ảnh gallery",
            description = "Cập nhật URL hoặc thứ tự hiển thị của một hình ảnh trong gallery"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy hình ảnh")
    })
    @PutMapping("/gallery/{galleryId}")
    @RequireAdmin
    public ResponseEntity<RestaurantResponseDTO> updateGalleryImage(
            @PathVariable Long galleryId,
            @Valid @RequestBody GalleryRequest request) {
        return ResponseEntity.ok(restaurantService.updateGalleryImage(galleryId, request));
    }

    /**
     * Xóa hình ảnh khỏi gallery
     */
    @Operation(
            summary = "Xóa hình ảnh gallery",
            description = "Xóa một hình ảnh khỏi gallery nhà hàng"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy hình ảnh")
    })
    @DeleteMapping("/gallery/{galleryId}")
    @RequireAdmin
    public ResponseEntity<RestaurantResponseDTO> deleteGalleryImage(@PathVariable Long galleryId) {
        return ResponseEntity.ok(restaurantService.deleteGalleryImage(galleryId));
    }

    /**
     * Sắp xếp lại thứ tự hình ảnh gallery
     */
    @Operation(
            summary = "Sắp xếp lại gallery",
            description = "Cập nhật thứ tự hiển thị của các hình ảnh trong gallery. " +
                    "Truyền vào danh sách ID theo thứ tự mong muốn."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sắp xếp thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy một trong các hình ảnh")
    })
    @PutMapping("/gallery/reorder")
    @RequireAdmin
    public ResponseEntity<RestaurantResponseDTO> reorderGalleryImages(
            @RequestBody List<Long> galleryIds) {
        return ResponseEntity.ok(restaurantService.reorderGalleryImages(galleryIds));
    }
}

