package com.foodorder.backend.category.dto.response;
import lombok.Data;

@Data
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private Integer displayOrder;
    private boolean hasChildren;
    private String slug;
}
