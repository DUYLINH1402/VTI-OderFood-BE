package com.foodorder.backend.repository;
import com.foodorder.backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByName( String name);

    List<Category> findByParentIdIsNull();

    List<Category> findByParentId(Long parentId);

    boolean existsById(Long id);
    Optional<Category> findBySlug(String slug);

}
