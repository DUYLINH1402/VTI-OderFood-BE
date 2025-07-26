// CategoryServiceImpl.java – Đã cập nhật để hỗ trợ slug
package com.foodorder.backend.category.service.impl;

import com.foodorder.backend.category.dto.request.CategoryRequest;
import com.foodorder.backend.category.dto.response.CategoryResponse;
import com.foodorder.backend.category.entity.Category;
import com.foodorder.backend.category.repository.CategoryRepository;
import com.foodorder.backend.category.service.CategoryService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ModelMapper modelMapper;

    private CategoryResponse mapToDto(Category category) {
        CategoryResponse dto = new CategoryResponse();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setDisplayOrder(category.getDisplayOrder());
        dto.setParentId(category.getParentId());
        dto.setSlug(category.getSlug()); // thêm slug vào DTO

        boolean hasChildren = category.getChildren() != null && !category.getChildren().isEmpty();
        dto.setHasChildren(hasChildren);
        return dto;
    }

    private Category mapToEntity(CategoryRequest request) {
        return modelMapper.map(request, Category.class);
    }

    private String generateSlug(String name) {
        String slug = Normalizer.normalize(name, Normalizer.Form.NFD) // bỏ dấu
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")       // lọc ký tự đặc biệt
                .toLowerCase(Locale.ENGLISH)                                                 // chuyển thường
                .replaceAll("[^a-z0-9\\s-]", "")                           // bỏ ký tự lạ
                .replaceAll("\\s+", "-")                                   // khoảng trắng → gạch ngang
                .replaceAll("-{2,}", "-")                                  // gộp gạch liền
                .replaceAll("^-|-$", "");                                  // xoá đầu đuôi gạch
        return slug;
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        Category category = mapToEntity(request);
        if (!StringUtils.hasText(request.getSlug())) {
            category.setSlug(generateSlug(category.getName()));
        } else {
            category.setSlug(request.getSlug());
        }
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

        // Cập nhật slug nếu cần
        if (StringUtils.hasText(request.getSlug())) {
            category.setSlug(request.getSlug());
        } else {
            category.setSlug(generateSlug(request.getName()));
        }

        Category updated = categoryRepository.save(category);
        return mapToDto(updated);
    }

    @Override
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    @Override
    public List<CategoryResponse> getRootCategories() {
        List<Category> roots = categoryRepository.findByParentIdIsNull();
        return roots.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public List<CategoryResponse> getCategoriesByParentId(Long parentId) {
        List<Category> categories = categoryRepository.findByParentId(parentId);
        return categories.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("CATEGORY_NOT_FOUND"));
        return mapToDto(category);
    }

    @Override
    public List<CategoryResponse> getCategoriesByParentSlug(String slug) {
        Category parent = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("PARENT_CATEGORY_NOT_FOUND"));
        return getCategoriesByParentId(parent.getId());
    }
}
