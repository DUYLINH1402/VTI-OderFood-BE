package com.foodorder.backend.restaurant.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * DTO Request để cập nhật thông tin nhà hàng
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request cập nhật thông tin nhà hàng")
public class RestaurantUpdateRequest {

    @NotBlank(message = "Tên nhà hàng không được để trống")
    @Size(max = 255, message = "Tên nhà hàng không được vượt quá 255 ký tự")
    @Schema(description = "Tên nhà hàng", example = "Nhà hàng ABC", required = true)
    private String name;

    @Size(max = 500, message = "URL logo không được vượt quá 500 ký tự")
    @Schema(description = "URL logo nhà hàng", example = "https://example.com/logo.png")
    private String logoUrl;

    @Size(max = 500, message = "Địa chỉ không được vượt quá 500 ký tự")
    @Schema(description = "Địa chỉ nhà hàng", example = "123 Nguyễn Văn Linh, Q7, TP.HCM")
    private String address;

    @Size(max = 20, message = "Số điện thoại không được vượt quá 20 ký tự")
    @Schema(description = "Số điện thoại liên hệ", example = "0901234567")
    private String phoneNumber;

    @Size(max = 500, message = "URL video không được vượt quá 500 ký tự")
    @Schema(description = "URL video giới thiệu", example = "https://youtube.com/watch?v=xxx")
    private String videoUrl;

    @Schema(description = "Mô tả chi tiết về nhà hàng")
    private String description;

    @Size(max = 100, message = "Giờ mở cửa không được vượt quá 100 ký tự")
    @Schema(description = "Giờ mở cửa", example = "07:00 - 22:00")
    private String openingHours;
}

