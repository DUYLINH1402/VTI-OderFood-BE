package com.foodorder.backend.service;


import com.foodorder.backend.dto.request.CategoryRequest;
import com.foodorder.backend.dto.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request);
    List<CategoryResponse> getAllCategories();
    CategoryResponse getCategoryById(Long id);
    CategoryResponse getCategoryBySlug(String slug);
    List<CategoryResponse> getCategoriesByParentSlug(String slug);
    CategoryResponse updateCategory(Long id, CategoryRequest request);
    void deleteCategory(Long id);

    List<CategoryResponse> getRootCategories();

    List<CategoryResponse> getCategoriesByParentId(Long parentId);
}

