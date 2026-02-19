package com.foodorder.backend.food.repository;

import com.foodorder.backend.food.entity.Food;
import com.foodorder.backend.food.entity.FoodStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Tìm kiếm món ăn với bộ lọc động cho Staff quản lý
     * Hỗ trợ lọc theo: tên, trạng thái, danh mục, trạng thái hoạt động
     */
    @Query("SELECT f FROM Food f LEFT JOIN FETCH f.category " +
            "WHERE (:name IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:status IS NULL OR f.status = :status) " +
            "AND (:categoryId IS NULL OR f.category.id = :categoryId) " +
            "AND (:isActive IS NULL OR f.isActive = :isActive)")
    Page<Food> findWithFilter(
            @Param("name") String name,
            @Param("status") FoodStatus status,
            @Param("categoryId") Long categoryId,
            @Param("isActive") Boolean isActive,
            Pageable pageable);

    /**
     * Đếm số lượng món ăn theo bộ lọc (dùng cho phân trang)
     */
    @Query("SELECT COUNT(f) FROM Food f " +
            "WHERE (:name IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:status IS NULL OR f.status = :status) " +
            "AND (:categoryId IS NULL OR f.category.id = :categoryId) " +
            "AND (:isActive IS NULL OR f.isActive = :isActive)")
    long countWithFilter(
            @Param("name") String name,
            @Param("status") FoodStatus status,
            @Param("categoryId") Long categoryId,
            @Param("isActive") Boolean isActive);

    // Thêm phương thức để tăng totalSold cho món ăn theo foodId
    @Modifying
    @Transactional
    @Query("UPDATE Food f SET f.totalSold = COALESCE(f.totalSold, 0) + :amount WHERE f.id = :foodId")
    void incrementTotalSold(@Param("foodId") Long foodId, @Param("amount") Integer amount);

    /**
     * Lấy tất cả món ăn đang hoạt động (dùng cho đồng bộ Elasticsearch)
     */
    @Query("SELECT f FROM Food f LEFT JOIN FETCH f.category WHERE f.isActive = true")
    List<Food> findAllByIsActiveTrue();
}
