package com.foodorder.backend.food.dto.response;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodResponse {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String imageUrl;
    private String categoryName; // Nếu có category
    private Long categoryId;
    private Long parentId;
    private Boolean isBestSeller;
    private Boolean isNew;
    private Boolean isFeatured;
    private String status;
    // Ghi chú lý do thay đổi trạng thái (hết hàng, bảo trì...)
    private String statusNote;
    private String slug;
    private List<String> images;
    private List<FoodVariantResponse> variants;

}
