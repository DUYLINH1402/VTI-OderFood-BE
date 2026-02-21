package com.foodorder.backend.restaurant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * DTO Response trả về thông tin đầy đủ của nhà hàng kèm danh sách ảnh gallery
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response chứa thông tin chi tiết nhà hàng")
public class RestaurantResponseDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID của nhà hàng", example = "1")
    private Long id;

    @Schema(description = "Tên nhà hàng", example = "Nhà hàng ABC")
    private String name;

    @Schema(description = "URL logo nhà hàng", example = "https://example.com/logo.png")
    private String logoUrl;

    @Schema(description = "Địa chỉ nhà hàng", example = "123 Nguyễn Văn Linh, Q7, TP.HCM")
    private String address;

    @Schema(description = "Số điện thoại liên hệ", example = "0901234567")
    private String phoneNumber;

    @Schema(description = "URL video giới thiệu", example = "https://youtube.com/watch?v=xxx")
    private String videoUrl;

    @Schema(description = "Mô tả chi tiết về nhà hàng")
    private String description;

    @Schema(description = "Giờ mở cửa", example = "07:00 - 22:00")
    private String openingHours;

    @Schema(description = "Danh sách URL hình ảnh gallery của nhà hàng")
    private List<GalleryItemDTO> galleries;

    /**
     * DTO cho từng item trong gallery
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Thông tin một hình ảnh trong gallery")
    public static class GalleryItemDTO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Schema(description = "ID của ảnh gallery", example = "1")
        private Long id;

        @Schema(description = "URL hình ảnh", example = "https://example.com/image1.jpg")
        private String imageUrl;

        @Schema(description = "Thứ tự hiển thị", example = "1")
        private Integer displayOrder;
    }
}

