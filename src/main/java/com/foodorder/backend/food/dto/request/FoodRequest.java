package com.foodorder.backend.food.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body để tạo/cập nhật món ăn")
public class FoodRequest {

    @Schema(description = "Tên món ăn", example = "Phở bò tái", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "Mô tả món ăn", example = "Phở bò tái với nước dùng đậm đà")
    private String description;

    @Schema(description = "Giá món ăn (VND)", example = "55000", requiredMode = Schema.RequiredMode.REQUIRED)
    private Double price;

    @Schema(description = "File hình ảnh món ăn", type = "string", format = "binary")
    private MultipartFile imageUrl;

    @Schema(description = "ID của danh mục chứa món ăn", example = "1")
    private Long categoryId;

    @Schema(description = "ID của món ăn cha (nếu có)", example = "2")
    private Long parentId;

    @Schema(description = "Đánh dấu món bán chạy", example = "true")
    private Boolean isBestSeller;

    @Schema(description = "Đánh dấu món mới", example = "true")
    private Boolean isNew;

    @Schema(description = "Đánh dấu món nổi bật", example = "false")
    private Boolean isFeatured;
}
