package com.foodorder.backend.food.repository;

import com.foodorder.backend.food.entity.Food;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FoodRepository extends JpaRepository<Food, Long> {
    Page<Food> findByIsNewTrue(Pageable pageable);

    Page<Food> findByIsFeaturedTrue(Pageable pageable);

    Page<Food> findByIsBestSellerTrue(Pageable pageable);

    Page<Food> findByCategoryId(Long categoryId, Pageable pageable);

    Optional<Food> findBySlug(String slug);

    // Thay đổi method để sử dụng trường id thay vì createdAt
    // ID tăng dần nên có thể sử dụng để lấy món ăn mới nhất
    List<Food> findTop6ByOrderByIdDesc();

    // Thêm phương thức đếm số lượng món ăn theo danh mục
    long countByCategoryId(Long categoryId);

    // Thêm phương thức eager load category để tránh lỗi lazy loading
    @Query("SELECT f FROM Food f LEFT JOIN FETCH f.category ORDER BY f.id DESC")
    List<Food> findTop6WithCategoryByOrderByIdDesc(Pageable pageable);
}