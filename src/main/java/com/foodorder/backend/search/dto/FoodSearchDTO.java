package com.foodorder.backend.search.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;

/**
 * DTO cho việc đồng bộ dữ liệu món ăn với Algolia Search
 * objectID là trường bắt buộc của Algolia, được map từ id trong MySQL
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FoodSearchDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Object ID của Algolia (map từ id của MySQL)
     * Đây là trường bắt buộc để Algolia nhận diện document
     */
    @JsonProperty("objectID")
    private String objectID;

    /**
     * Tên món ăn - searchable field chính
     */
    private String name;

    /**
     * Mô tả món ăn - searchable field phụ
     */
    private String description;

    /**
     * Giá món ăn (VND)
     */
    private Double price;

    /**
     * URL hình ảnh món ăn
     */
    private String imageUrl;

    /**
     * Slug của món ăn để tạo URL thân thiện
     */
    private String slug;

    /**
     * Tên danh mục chứa món ăn
     */
    private String categoryName;

    /**
     * ID của danh mục
     */
    private Long categoryId;

    /**
     * Trạng thái món ăn (AVAILABLE/UNAVAILABLE)
     */
    private String status;

    /**
     * Đánh dấu món bán chạy
     */
    private Boolean isBestSeller;

    /**
     * Đánh dấu món mới
     */
    private Boolean isNew;

    /**
     * Đánh dấu món nổi bật
     */
    private Boolean isFeatured;
}

