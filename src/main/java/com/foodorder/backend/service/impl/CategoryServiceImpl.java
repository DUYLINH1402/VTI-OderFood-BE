package com.foodorder.backend.service.impl;
import com.foodorder.backend.dto.request.CategoryRequest;
import com.foodorder.backend.dto.response.CategoryResponse;
import com.foodorder.backend.entity.Category;
import com.foodorder.backend.repository.CategoryRepository;
import com.foodorder.backend.service.CategoryService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ModelMapper modelMapper;

    private CategoryResponse mapToDto(Category category) {
        return modelMapper.map(category, CategoryResponse.class); //  dùng ModelMapper
    }
    private Category mapToEntity(CategoryRequest request) {
        return modelMapper.map(request, Category.class); //  dùng ModelMapper
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = mapToEntity(request);
        Category saved = categoryRepository.save(category);
        return mapToDto(saved);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return mapToDto(category);
    }

    @Override
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        Category updated = categoryRepository.save(category);
        return mapToDto(updated);
    }

    @Override
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
