package com.foodorder.backend.chatbot.service;

import com.foodorder.backend.category.entity.Category;
import com.foodorder.backend.category.repository.CategoryRepository;
import com.foodorder.backend.food.entity.Food;
import com.foodorder.backend.food.repository.FoodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Service để lấy thông tin thực đơn từ database cho chatbot
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MenuInfoService {

    private final FoodRepository foodRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Lấy thông tin tổng quan về thực đơn
     */
    public String getMenuOverview() {
        try {
            // Lấy tổng số món ăn
            long totalFoods = foodRepository.count();

            // Lấy danh sách category và số lượng món ăn trong mỗi category
            List<Category> categories = categoryRepository.findAll();

            StringBuilder menuInfo = new StringBuilder();
            menuInfo.append("🍽️ **THÔNG TIN THỰC ĐƠN** 🍽️\n\n");
            menuInfo.append("Chúng tôi hiện có **").append(totalFoods).append(" món ăn** đa dạng được phân loại theo:\n\n");

            // Thêm thông tin từng danh mục
            for (Category category : categories) {
                long foodCount = foodRepository.countByCategoryId(category.getId());
                if (foodCount > 0) {
                    menuInfo.append("🔸 **").append(category.getName()).append("**: ")
                            .append(foodCount).append(" món\n");
                }
            }

            // Thêm thông tin món nổi bật
            menuInfo.append("\n**MỚN NỔI BẬT:**\n");

            // Món bán chạy
            List<Food> bestSellers = foodRepository.findByIsBestSellerTrue(PageRequest.of(0, 5)).getContent();
            if (!bestSellers.isEmpty()) {
                menuInfo.append("🌟 **Món bán chạy**: ");
                menuInfo.append(bestSellers.stream()
                        .map(food -> food.getName() + " (" + formatPrice(food.getPrice()) + ")")
                        .collect(Collectors.joining(", ")));
                menuInfo.append("\n");
            }

            // Món mới
            List<Food> newFoods = foodRepository.findByIsNewTrue(PageRequest.of(0, 5)).getContent();
            if (!newFoods.isEmpty()) {
                menuInfo.append("🆕 **Món mới**: ");
                menuInfo.append(newFoods.stream()
                        .map(food -> food.getName() + " (" + formatPrice(food.getPrice()) + ")")
                        .collect(Collectors.joining(", ")));
                menuInfo.append("\n");
            }

            // Món đặc sắc
            List<Food> featuredFoods = foodRepository.findByIsFeaturedTrue(PageRequest.of(0, 5)).getContent();
            if (!featuredFoods.isEmpty()) {
                menuInfo.append("⭐ **Món đặc sắc**: ");
                menuInfo.append(featuredFoods.stream()
                        .map(food -> food.getName() + " (" + formatPrice(food.getPrice()) + ")")
                        .collect(Collectors.joining(", ")));
                menuInfo.append("\n");
            }

            menuInfo.append("\n💡 **Lưu ý**: Tất cả món ăn đều được chuẩn bị từ nguyên liệu tươi ngon, ");
            menuInfo.append("đảm bảo vệ sinh an toàn thực phẩm và có thể tùy chỉnh theo yêu cầu của quý khách!");

            return menuInfo.toString();

        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin thực đơn: {}", e.getMessage());
            return "Xin lỗi, hiện tại không thể lấy thông tin thực đơn. Vui lòng liên hệ hotline để được hỗ trợ!";
        }
    }

    /**
     * Tìm kiếm món ăn theo từ khóa
     */
    public String searchFoodsByKeyword(String keyword) {
        try {
            List<Food> allFoods = foodRepository.findAll();
            List<Food> matchedFoods = allFoods.stream()
                    .filter(food -> food.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                                  (food.getDescription() != null && food.getDescription().toLowerCase().contains(keyword.toLowerCase())))
                    .limit(10) // Giới hạn 10 kết quả
                    .toList();

            if (matchedFoods.isEmpty()) {
                return "🔍 Không tìm thấy món ăn nào phù hợp với từ khóa: **" + keyword + "**\n\n" +
                       "Bạn có thể thử tìm kiếm với các từ khóa khác hoặc xem thực đơn đầy đủ của chúng tôi!";
            }

            StringBuilder result = new StringBuilder();
            result.append("🔍 **KẾT QUẢ TÌM KIẾM**: \"").append(keyword).append("\"\n\n");
            result.append("Tìm thấy **").append(matchedFoods.size()).append(" món ăn** phù hợp:\n\n");

            for (Food food : matchedFoods) {
                result.append("🍽️ **").append(food.getName()).append("**\n");
                result.append("💰 Giá: ").append(formatPrice(food.getPrice())).append("\n");
                if (food.getDescription() != null && !food.getDescription().trim().isEmpty()) {
                    result.append("📝 ").append(food.getDescription()).append("\n");
                }

                // Thêm nhãn đặc biệt
                StringBuilder badges = new StringBuilder();
                if (Boolean.TRUE.equals(food.getIsBestSeller())) badges.append("🌟 Bán chạy ");
                if (Boolean.TRUE.equals(food.getIsNew())) badges.append("🆕 Mới ");
                if (Boolean.TRUE.equals(food.getIsFeatured())) badges.append("⭐ Đặc sắc ");

                if (!badges.isEmpty()) {
                    result.append("🏷️ ").append(badges.toString().trim()).append("\n");
                }
                result.append("\n");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm món ăn: {}", e.getMessage());
            return "Xin lỗi, có lỗi xảy ra khi tìm kiếm. Vui lòng thử lại sau!";
        }
    }

    /**
     * Lấy thông tin món ăn theo danh mục
     */
    public String getFoodsByCategory(String categoryName) {
        try {
            // Tìm category theo tên
            List<Category> categories = categoryRepository.findAll();
            Category matchedCategory = categories.stream()
                    .filter(cat -> cat.getName().toLowerCase().contains(categoryName.toLowerCase()))
                    .findFirst()
                    .orElse(null);

            if (matchedCategory == null) {
                return "❌ Không tìm thấy danh mục: **" + categoryName + "**\n\n" +
                       "Các danh mục hiện có: " + categories.stream()
                               .map(Category::getName)
                               .collect(Collectors.joining(", "));
            }

            List<Food> foods = foodRepository.findByCategoryId(matchedCategory.getId(), PageRequest.of(0, 20))
                    .getContent();

            if (foods.isEmpty()) {
                return "📂 Danh mục **" + matchedCategory.getName() + "** hiện chưa có món ăn nào.";
            }

            StringBuilder result = new StringBuilder();
            result.append("📂 **DANH MỤC: ").append(matchedCategory.getName().toUpperCase()).append("**\n\n");
            result.append("Có **").append(foods.size()).append(" món ăn** trong danh mục này:\n\n");

            for (Food food : foods) {
                result.append("🍽️ **").append(food.getName()).append("** - ")
                      .append(formatPrice(food.getPrice())).append("\n");

                if (food.getDescription() != null && !food.getDescription().trim().isEmpty()) {
                    result.append("   📝 ").append(food.getDescription()).append("\n");
                }
                result.append("\n");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("Lỗi khi lấy món ăn theo danh mục: {}", e.getMessage());
            return "Xin lỗi, có lỗi xảy ra khi lấy thông tin danh mục. Vui lòng thử lại sau!";
        }
    }

    /**
     * Format giá tiền
     */
    private String formatPrice(BigDecimal price) {
        if (price == null) {
            return "Liên hệ";
        }

        try {
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            return formatter.format(price).replace("₫", "VNĐ");
        } catch (Exception e) {
            log.warn("Lỗi khi format giá: {}", e.getMessage());
            return price.toString() + " VNĐ";
        }
    }
}
