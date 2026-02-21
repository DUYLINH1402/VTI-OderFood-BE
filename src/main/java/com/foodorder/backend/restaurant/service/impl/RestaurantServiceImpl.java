package com.foodorder.backend.restaurant.service.impl;

import com.foodorder.backend.config.CacheConfig;
import com.foodorder.backend.exception.ResourceNotFoundException;
import com.foodorder.backend.restaurant.dto.GalleryRequest;
import com.foodorder.backend.restaurant.dto.RestaurantResponseDTO;
import com.foodorder.backend.restaurant.dto.RestaurantUpdateRequest;
import com.foodorder.backend.restaurant.entity.RestaurantGallery;
import com.foodorder.backend.restaurant.entity.RestaurantInfo;
import com.foodorder.backend.restaurant.repository.RestaurantGalleryRepository;
import com.foodorder.backend.restaurant.repository.RestaurantInfoRepository;
import com.foodorder.backend.restaurant.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation cho quản lý thông tin nhà hàng
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantInfoRepository restaurantInfoRepository;
    private final RestaurantGalleryRepository restaurantGalleryRepository;

    // ID mặc định của nhà hàng (hiện tại chỉ có một cơ sở)
    private static final Long DEFAULT_RESTAURANT_ID = 1L;

    /**
     * Lấy thông tin chi tiết nhà hàng
     * Cache với TTL 30 phút vì thông tin nhà hàng ít thay đổi
     *
     * @return RestaurantResponseDTO chứa thông tin nhà hàng và danh sách gallery
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CacheConfig.RESTAURANT_INFO_CACHE, key = "'default'")
    public RestaurantResponseDTO getRestaurantDetails() {
        log.info("Lấy thông tin nhà hàng với ID: {}", DEFAULT_RESTAURANT_ID);

        RestaurantInfo restaurantInfo = restaurantInfoRepository.findById(DEFAULT_RESTAURANT_ID)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy thông tin nhà hàng", "RESTAURANT_NOT_FOUND"));

        return mapToDTO(restaurantInfo);
    }

    /**
     * Convert entity sang DTO
     */
    private RestaurantResponseDTO mapToDTO(RestaurantInfo entity) {
        List<RestaurantResponseDTO.GalleryItemDTO> galleryDTOs = Collections.emptyList();

        if (entity.getGalleries() != null && !entity.getGalleries().isEmpty()) {
            galleryDTOs = entity.getGalleries().stream()
                    .map(this::mapGalleryToDTO)
                    .collect(Collectors.toList());
        }

        return RestaurantResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .logoUrl(entity.getLogoUrl())
                .address(entity.getAddress())
                .phoneNumber(entity.getPhoneNumber())
                .videoUrl(entity.getVideoUrl())
                .description(entity.getDescription())
                .openingHours(entity.getOpeningHours())
                .galleries(galleryDTOs)
                .build();
    }

    /**
     * Convert gallery entity sang DTO
     */
    private RestaurantResponseDTO.GalleryItemDTO mapGalleryToDTO(RestaurantGallery gallery) {
        return RestaurantResponseDTO.GalleryItemDTO.builder()
                .id(gallery.getId())
                .imageUrl(gallery.getImageUrl())
                .displayOrder(gallery.getDisplayOrder())
                .build();
    }

    // ==================== ADMIN APIs ====================

    /**
     * Cập nhật thông tin nhà hàng
     * Xóa cache sau khi cập nhật
     */
    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.RESTAURANT_INFO_CACHE, allEntries = true)
    public RestaurantResponseDTO updateRestaurantInfo(RestaurantUpdateRequest request) {
        log.info("Admin cập nhật thông tin nhà hàng");

        RestaurantInfo restaurantInfo = getRestaurantEntity();

        // Cập nhật thông tin
        restaurantInfo.setName(request.getName());
        restaurantInfo.setLogoUrl(request.getLogoUrl());
        restaurantInfo.setAddress(request.getAddress());
        restaurantInfo.setPhoneNumber(request.getPhoneNumber());
        restaurantInfo.setVideoUrl(request.getVideoUrl());
        restaurantInfo.setDescription(request.getDescription());
        restaurantInfo.setOpeningHours(request.getOpeningHours());

        RestaurantInfo saved = restaurantInfoRepository.save(restaurantInfo);
        log.info("Đã cập nhật thông tin nhà hàng thành công");

        return mapToDTO(saved);
    }

    /**
     * Thêm hình ảnh vào gallery
     */
    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.RESTAURANT_INFO_CACHE, allEntries = true)
    public RestaurantResponseDTO addGalleryImage(GalleryRequest request) {
        log.info("Admin thêm hình ảnh vào gallery");

        RestaurantInfo restaurantInfo = getRestaurantEntity();

        // Tạo gallery mới
        RestaurantGallery gallery = RestaurantGallery.builder()
                .imageUrl(request.getImageUrl())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : getNextDisplayOrder(restaurantInfo))
                .restaurantInfo(restaurantInfo)
                .build();

        restaurantGalleryRepository.save(gallery);
        log.info("Đã thêm hình ảnh gallery với ID: {}", gallery.getId());

        // Refresh entity để lấy danh sách gallery mới
        return getRestaurantDetails();
    }

    /**
     * Cập nhật hình ảnh gallery
     */
    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.RESTAURANT_INFO_CACHE, allEntries = true)
    public RestaurantResponseDTO updateGalleryImage(Long galleryId, GalleryRequest request) {
        log.info("Admin cập nhật gallery ID: {}", galleryId);

        RestaurantGallery gallery = restaurantGalleryRepository.findById(galleryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy hình ảnh gallery", "GALLERY_NOT_FOUND"));

        gallery.setImageUrl(request.getImageUrl());
        if (request.getDisplayOrder() != null) {
            gallery.setDisplayOrder(request.getDisplayOrder());
        }

        restaurantGalleryRepository.save(gallery);
        log.info("Đã cập nhật gallery ID: {}", galleryId);

        return getRestaurantDetails();
    }

    /**
     * Xóa hình ảnh khỏi gallery
     */
    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.RESTAURANT_INFO_CACHE, allEntries = true)
    public RestaurantResponseDTO deleteGalleryImage(Long galleryId) {
        log.info("Admin xóa gallery ID: {}", galleryId);

        RestaurantGallery gallery = restaurantGalleryRepository.findById(galleryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy hình ảnh gallery", "GALLERY_NOT_FOUND"));

        restaurantGalleryRepository.delete(gallery);
        log.info("Đã xóa gallery ID: {}", galleryId);

        return getRestaurantDetails();
    }

    /**
     * Cập nhật thứ tự hiển thị của các hình ảnh gallery
     */
    @Override
    @Transactional
    @CacheEvict(value = CacheConfig.RESTAURANT_INFO_CACHE, allEntries = true)
    public RestaurantResponseDTO reorderGalleryImages(List<Long> galleryIds) {
        log.info("Admin sắp xếp lại thứ tự gallery");

        for (int i = 0; i < galleryIds.size(); i++) {
            Long galleryId = galleryIds.get(i);
            RestaurantGallery gallery = restaurantGalleryRepository.findById(galleryId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy hình ảnh gallery với ID: " + galleryId, "GALLERY_NOT_FOUND"));

            gallery.setDisplayOrder(i + 1);
            restaurantGalleryRepository.save(gallery);
        }

        log.info("Đã sắp xếp lại {} hình ảnh gallery", galleryIds.size());
        return getRestaurantDetails();
    }

    // ==================== Helper Methods ====================

    /**
     * Lấy entity nhà hàng
     */
    private RestaurantInfo getRestaurantEntity() {
        return restaurantInfoRepository.findById(DEFAULT_RESTAURANT_ID)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy thông tin nhà hàng", "RESTAURANT_NOT_FOUND"));
    }

    /**
     * Lấy thứ tự hiển thị tiếp theo cho gallery mới
     */
    private Integer getNextDisplayOrder(RestaurantInfo restaurantInfo) {
        if (restaurantInfo.getGalleries() == null || restaurantInfo.getGalleries().isEmpty()) {
            return 1;
        }
        return restaurantInfo.getGalleries().stream()
                .mapToInt(g -> g.getDisplayOrder() != null ? g.getDisplayOrder() : 0)
                .max()
                .orElse(0) + 1;
    }
}

