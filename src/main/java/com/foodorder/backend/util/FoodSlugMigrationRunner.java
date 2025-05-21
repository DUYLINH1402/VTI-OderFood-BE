package com.foodorder.backend.util;

import com.foodorder.backend.entity.Food;
import com.foodorder.backend.repository.FoodRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

//@Component
public class FoodSlugMigrationRunner implements CommandLineRunner {

    private final FoodRepository foodRepository;

    public FoodSlugMigrationRunner(FoodRepository foodRepository) {
        this.foodRepository = foodRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        List<Food> foods = foodRepository.findAll();
        List<String> existingSlugs = new ArrayList<>();
        int count = 0;

        for (Food food : foods) {
            if (!StringUtils.hasText(food.getSlug())) {
                String slug = generateUniqueSlug(food.getName(), existingSlugs);
                food.setSlug(slug);
                count++;
            } else {
                existingSlugs.add(food.getSlug());
            }
        }

        if (count > 0) {
            foodRepository.saveAll(foods);
            System.out.println("✅ Đã cập nhật slug cho " + count + " món ăn.");
        } else {
            System.out.println("✅ Tất cả món ăn đã có slug.");
        }
    }

    private String generateSlug(String name) {
        // Thay thế đ -> d trước khi normalize
        name = name.replace("đ", "d").replace("Đ", "D");

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

        while (foodRepository.findBySlug(slug).isPresent() || existingSlugs.contains(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }

        existingSlugs.add(slug);
        return slug;
    }
}

