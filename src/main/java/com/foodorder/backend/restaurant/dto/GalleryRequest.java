package com.foodorder.backend.restaurant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO Request để thêm/cập nhật hình ảnh gallery
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request thêm/cập nhật hình ảnh gallery")
public class GalleryRequest {

    @NotBlank(message = "URL hình ảnh không được để trống")
    @Size(max = 500, message = "URL hình ảnh không được vượt quá 500 ký tự")
    @Schema(description = "URL hình ảnh", example = "https://example.com/image.jpg", required = true)
    private String imageUrl;

    @Schema(description = "Thứ tự hiển thị", example = "1")
    private Integer displayOrder;
}

