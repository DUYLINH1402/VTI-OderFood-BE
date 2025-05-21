// COMPONENT TẠO SLUG CATEGORY TRONG CSDL

package com.foodorder.backend.util;

import com.foodorder.backend.entity.Category;
import com.foodorder.backend.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.*;
import java.util.Locale;

//@Component
public class SlugMigrationRunner implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    public SlugMigrationRunner(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        List<Category> categories = categoryRepository.findAll();
        List<String> existingSlugs = new ArrayList<>();
        int count = 0;

        for (Category category : categories) {
            if (!StringUtils.hasText(category.getSlug())) {
                String slug = generateUniqueSlug(category.getName(), existingSlugs);
                category.setSlug(slug);
                count++;
            } else {
                existingSlugs.add(category.getSlug());
            }
        }

        if (count > 0) {
            categoryRepository.saveAll(categories);
            System.out.println("✅ Đã cập nhật slug cho " + count + " danh mục.");
        } else {
            System.out.println("✅ Tất cả danh mục đã có slug.");
        }
    }

    private String generateSlug(String name) {
        return Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase(Locale.ENGLISH)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-{2,}", "-")
                .replaceAll("^-|-$", "");
    }

    private String generateUniqueSlug(String baseName, List<String> existingSlugs) {
        String baseSlug = generateSlug(baseName);
        String slug = baseSlug;
        int counter = 1;

        while (categoryRepository.findBySlug(slug).isPresent() || existingSlugs.contains(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }

        existingSlugs.add(slug);
        return slug;
    }
}
