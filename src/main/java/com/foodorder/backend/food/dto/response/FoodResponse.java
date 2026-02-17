package com.foodorder.backend.food.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response chứa thông tin chi tiết món ăn")
public class FoodResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID của món ăn", example = "1")
    private Long id;

    @Schema(description = "Tên món ăn", example = "Phở bò tái")
    private String name;

    @Schema(description = "Mô tả món ăn", example = "Phở bò tái với nước dùng đậm đà")
    private String description;

    @Schema(description = "Giá món ăn (VND)", example = "55000")
    private Double price;

    @Schema(description = "URL hình ảnh chính", example = "https://example.com/pho.jpg")
    private String imageUrl;

    @Schema(description = "Tên danh mục chứa món ăn", example = "Món chính")
    private String categoryName;

    @Schema(description = "ID danh mục", example = "1")
    private Long categoryId;

    @Schema(description = "ID món ăn cha (nếu có)", example = "2")
    private Long parentId;

    @Schema(description = "Đánh dấu món bán chạy", example = "true")
    private Boolean isBestSeller;

    @Schema(description = "Đánh dấu món mới", example = "true")
    private Boolean isNew;

    @Schema(description = "Đánh dấu món nổi bật", example = "false")
    private Boolean isFeatured;

    @Schema(description = "Trạng thái món ăn", example = "AVAILABLE", allowableValues = {"AVAILABLE", "UNAVAILABLE"})
    private String status;

    @Schema(description = "Tổng số lượng đã bán", example = "150")
    private Integer totalSold;

    @Schema(description = "Tổng số lượt like", example = "25")
    private Integer totalLikes;

    @Schema(description = "Tổng số lượt share", example = "10")
    private Integer totalShares;

    @Schema(description = "Ghi chú trạng thái (VD: lý do hết hàng)", example = "Tạm hết nguyên liệu")
    private String statusNote;

    @Schema(description = "Dữ liệu được bảo vệ (chỉ SUPER_ADMIN có quyền sửa/xóa)", example = "false")
    private Boolean isProtected;

    @Schema(description = "Slug của món ăn (dùng cho URL)", example = "pho-bo-tai")
    private String slug;

    @Schema(description = "Danh sách URL hình ảnh bổ sung")
    private List<String> images;

    @Schema(description = "Danh sách các biến thể của món ăn (size, topping...)")
    private List<FoodVariantResponse> variants;

}
