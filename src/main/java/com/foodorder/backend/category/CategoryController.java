package com.foodorder.backend.category;

import com.foodorder.backend.category.dto.request.CategoryRequest;
import com.foodorder.backend.category.dto.response.CategoryResponse;
import com.foodorder.backend.category.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin("*")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // Tạo danh mục mới
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.created(URI.create("/api/categories/" + response.getId())).body(response);
    }

    // Lấy danh sách danh mục cha
    @GetMapping("/roots")
    public ResponseEntity<List<CategoryResponse>> getRootCategories() {
        return ResponseEntity.ok(categoryService.getRootCategories());
    }

    // Lấy danh sách danh mục con
    @GetMapping("/by-parent/{parentId}")
    public ResponseEntity<List<CategoryResponse>> getByParentId(@PathVariable Long parentId) {
        return ResponseEntity.ok(categoryService.getCategoriesByParentId(parentId));
    }
    // Lấy danh sách danh mục con bằng slug
    @GetMapping("/by-parent-slug/{slug}")
    public ResponseEntity<List<CategoryResponse>> getByParentSlug(@PathVariable String slug) {
        CategoryResponse parent = categoryService.getCategoryBySlug(slug);
        return ResponseEntity.ok(categoryService.getCategoriesByParentId(parent.getId()));
    }

    // Lấy danh sách danh mục
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    // Lấy chi tiết danh mục
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    // Lấy chi tiết danh mục bằng slug
    @GetMapping("/slug/{slug}")
    public ResponseEntity<CategoryResponse> getCategoryBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(categoryService.getCategoryBySlug(slug));
    }

    // Cập nhật danh mục
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id, @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }


    // Xoá danh mục
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
