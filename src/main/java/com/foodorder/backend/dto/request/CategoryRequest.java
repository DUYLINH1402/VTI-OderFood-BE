package com.foodorder.backend.dto.request;
import lombok.Data;

@Data
public class CategoryRequest {
    private String name;
    private String description;
    private Long parentId;
    private Integer displayOrder;
    private String slug;

}
