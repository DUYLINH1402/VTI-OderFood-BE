package com.foodorder.backend.dto.request;


import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodRequest {
    private String name;
    private String description;
    private Double price;
    private MultipartFile imageUrl;
    private Long categoryId; // ID của danh mục (nếu có)
    private Long parentId;
    private Boolean isBestSeller;
    private Boolean isNew;
    private Boolean isFeatured;
}
