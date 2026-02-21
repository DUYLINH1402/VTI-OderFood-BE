package com.foodorder.backend.restaurant.service;

import com.foodorder.backend.restaurant.dto.GalleryRequest;
import com.foodorder.backend.restaurant.dto.RestaurantResponseDTO;
import com.foodorder.backend.restaurant.dto.RestaurantUpdateRequest;

import java.util.List;

/**
 * Service interface cho quản lý thông tin nhà hàng
 */
public interface RestaurantService {

    /**
     * Lấy thông tin chi tiết nhà hàng
     * Hiện tại chỉ có một cơ sở (ID = 1)
     *
     * @return RestaurantResponseDTO chứa thông tin nhà hàng và danh sách gallery
     */
    RestaurantResponseDTO getRestaurantDetails();

    // ==================== ADMIN APIs ====================

    /**
     * Cập nhật thông tin nhà hàng
     *
     * @param request Thông tin cập nhật
     * @return RestaurantResponseDTO sau khi cập nhật
     */
    RestaurantResponseDTO updateRestaurantInfo(RestaurantUpdateRequest request);

    /**
     * Thêm hình ảnh vào gallery
     *
     * @param request Thông tin hình ảnh
     * @return RestaurantResponseDTO sau khi thêm
     */
    RestaurantResponseDTO addGalleryImage(GalleryRequest request);

    /**
     * Cập nhật hình ảnh gallery
     *
     * @param galleryId ID của gallery item
     * @param request Thông tin cập nhật
     * @return RestaurantResponseDTO sau khi cập nhật
     */
    RestaurantResponseDTO updateGalleryImage(Long galleryId, GalleryRequest request);

    /**
     * Xóa hình ảnh khỏi gallery
     *
     * @param galleryId ID của gallery item cần xóa
     * @return RestaurantResponseDTO sau khi xóa
     */
    RestaurantResponseDTO deleteGalleryImage(Long galleryId);

    /**
     * Cập nhật thứ tự hiển thị của các hình ảnh gallery
     *
     * @param galleryIds Danh sách ID theo thứ tự mới
     * @return RestaurantResponseDTO sau khi cập nhật
     */
    RestaurantResponseDTO reorderGalleryImages(List<Long> galleryIds);
}

