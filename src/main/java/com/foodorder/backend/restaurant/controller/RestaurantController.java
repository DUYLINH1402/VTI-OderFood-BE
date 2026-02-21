package com.foodorder.backend.restaurant.controller;

import com.foodorder.backend.restaurant.dto.RestaurantResponseDTO;
import com.foodorder.backend.restaurant.service.RestaurantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * Controller xử lý API public cho thông tin nhà hàng
 */
@RestController
@RequestMapping("/api/v1/public/restaurant")
@RequiredArgsConstructor
@Tag(name = "Restaurant", description = "API công khai để lấy thông tin nhà hàng")
public class RestaurantController {

    private final RestaurantService restaurantService;

    /**
     * Lấy thông tin chi tiết nhà hàng
     * API Public - Không cần đăng nhập
     */
    @Operation(
            summary = "Lấy thông tin nhà hàng",
            description = "Lấy thông tin chi tiết nhà hàng bao gồm tên, địa chỉ, số điện thoại, mô tả, giờ mở cửa và danh sách hình ảnh gallery"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Thành công",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RestaurantResponseDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Không tìm thấy thông tin nhà hàng",
                    content = @Content
            )
    })
    @GetMapping
    public ResponseEntity<RestaurantResponseDTO> getRestaurantDetails() {
        RestaurantResponseDTO response = restaurantService.getRestaurantDetails();

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(30, TimeUnit.MINUTES).cachePublic())
                .body(response);
    }
}

