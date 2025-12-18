package com.foodorder.backend.chatbot.service;

import com.foodorder.backend.chatbot.entity.KnowledgeBase;
import com.foodorder.backend.chatbot.repository.KnowledgeBaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service xử lý hệ thống RAG (Retrieval-Augmented Generation)
 * Tìm kiếm và truy xuất thông tin từ knowledge base, Data Base để cung cấp context cho chatbot
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RAGService {

    private final KnowledgeBaseRepository knowledgeBaseRepository;
    private final MenuInfoService menuInfoService; // Thêm dependency

    @Value("${chatbot.context.similarity-threshold:0.7}")
    private Double similarityThreshold;

    /**
     * Tìm kiếm context phù hợp từ knowledge base cho câu hỏi của user
     */
    public String retrieveRelevantContext(String userMessage) {

        try {
            // Kiểm tra xem có phải câu hỏi về thực đơn không
            if (isMenuRelatedQuery(userMessage)) {
                return getMenuContext(userMessage);
            }

            // Trích xuất từ khóa từ tin nhắn user
            List<String> keywords = extractKeywords(userMessage);

            // Tìm kiếm knowledge base với từ khóa
            List<KnowledgeBase> relevantKnowledge = searchKnowledgeBase(keywords);

            if (relevantKnowledge.isEmpty()) {
                return "";
            }

            // Xây dựng context từ kết quả tìm kiếm
            String context = buildContextFromKnowledge(relevantKnowledge);
//            log.info("Đã tìm thấy {} mục knowledge phù hợp", relevantKnowledge.size());

            return context;

        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm context: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Kiểm tra xem có phải câu hỏi về thực đơn không
     */
    private boolean isMenuRelatedQuery(String message) {
        if (message == null || message.trim().isEmpty()) {
            return false;
        }

        String lowerMessage = message.toLowerCase();

        // Các từ khóa liên quan đến thực đơn
        String[] menuKeywords = {
            "thực đơn", "menu", "món ăn", "món", "đồ ăn", "food",
            "có món gì", "món nào", "ăn gì", "tìm món", "xem món",
            "bán gì", "phục vụ gì", "có bán", "danh sách món"
        };

        for (String keyword : menuKeywords) {
            if (lowerMessage.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Lấy context về thực đơn từ MenuInfoService
     */
    private String getMenuContext(String userMessage) {
        try {
            String menuInfo = menuInfoService.getMenuOverview();

            // Kiểm tra xem có từ khóa tìm kiếm cụ thể không
            String searchKeyword = extractSearchKeyword(userMessage);
            if (searchKeyword != null && !searchKeyword.isEmpty()) {
                String searchResult = menuInfoService.searchFoodsByKeyword(searchKeyword);
                return "THÔNG TIN THỰC ĐƠN:\n\n" + menuInfo + "\n\n" +
                       "KẾT QUẢ TÌM KIẾM:\n" + searchResult;
            }

            return "THÔNG TIN THỰC ĐƠN:\n\n" + menuInfo;

        } catch (Exception e) {
            log.error("Lỗi khi lấy thông tin thực đơn: {}", e.getMessage());
            return "THÔNG TIN THỰC ĐƠN:\n\nXin lỗi, hiện tại không thể lấy thông tin thực đơn. Vui lòng liên hệ nhân viên để được hỗ trợ!";
        }
    }

    /**
     * Trích xuất từ khóa tìm kiếm cụ thể từ câu hỏi về thực đơn
     */
    private String extractSearchKeyword(String message) {
        String lowerMessage = message.toLowerCase();

        // Tìm các mẫu câu hỏi có chứa từ khóa tìm kiếm
        String[] searchPatterns = {
            "tìm món", "có món", "món nào", "bán món",
            "phở", "pizza", "cơm", "bún", "bánh", "trà", "cà phê"
        };

        for (String pattern : searchPatterns) {
            if (lowerMessage.contains(pattern)) {
                // Trích xuất từ khóa sau pattern
                int index = lowerMessage.indexOf(pattern);
                if (index != -1) {
                    String after = lowerMessage.substring(index + pattern.length()).trim();
                    if (!after.isEmpty()) {
                        // Lấy từ đầu tiên sau pattern
                        String[] words = after.split("\\s+");
                        if (words.length > 0 && words[0].length() > 2) {
                            return words[0];
                        }
                    }
                    return pattern;
                }
            }
        }

        return null;
    }

    /**
     * Trích xuất từ khóa từ tin nhắn user
     */
    private List<String> extractKeywords(String message) {
        if (message == null || message.trim().isEmpty()) {
            return new ArrayList<>();
        }

        String normalizedMessage = message.toLowerCase().trim();

        // Danh sách từ khóa quan trọng cho nhà hàng
        Map<String, List<String>> keywordCategories = Map.of(
            "menu", Arrays.asList("thực đơn", "món ăn", "menu", "món", "đồ ăn", "food", "dish"),
            "order", Arrays.asList("đặt hàng", "order", "giao hàng", "delivery", "ship"),
            "payment", Arrays.asList("thanh toán", "payment", "pay", "tiền", "giá", "price", "cost"),
            "time", Arrays.asList("giờ", "time", "mở cửa", "đóng cửa", "hoạt động"),
            "location", Arrays.asList("địa chỉ", "chỗ", "location", "address", "ở đâu"),
            "promotion", Arrays.asList("khuyến mãi", "giảm giá", "promotion", "discount", "sale"),
            "contact", Arrays.asList("liên hệ", "contact", "phone", "email", "hotline")
        );

        List<String> foundKeywords = new ArrayList<>();

        // Tìm từ khóa trong các danh mục
        for (Map.Entry<String, List<String>> entry : keywordCategories.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (normalizedMessage.contains(keyword)) {
                    foundKeywords.add(keyword);
                }
            }
        }

        // Nếu không tìm thấy từ khóa đặc biệt, tách từ từ tin nhắn
        if (foundKeywords.isEmpty()) {
            String[] words = normalizedMessage.split("\\s+");
            for (String word : words) {
                if (word.length() > 3) { // Chỉ lấy từ có độ dài > 3
                    foundKeywords.add(word);
                }
            }
        }

        return foundKeywords.stream().distinct().collect(Collectors.toList());
    }

    /**
     * Tìm kiếm knowledge base với danh sách từ khóa
     */
    private List<KnowledgeBase> searchKnowledgeBase(List<String> keywords) {
        if (keywords.isEmpty()) {
            return knowledgeBaseRepository.findHighPriorityKnowledge(5);
        }

        Set<KnowledgeBase> results = new HashSet<>();

        // Tìm kiếm với từng từ khóa
        for (String keyword : keywords) {
            List<KnowledgeBase> matches = knowledgeBaseRepository.searchByKeyword(keyword);
            results.addAll(matches);
        }

        // Nếu có nhiều từ khóa, thử tìm kiếm kết hợp
        if (keywords.size() >= 2) {
            for (int i = 0; i < keywords.size() - 1; i++) {
                for (int j = i + 1; j < keywords.size(); j++) {
                    List<KnowledgeBase> combinedMatches = knowledgeBaseRepository
                        .searchByMultipleKeywords(keywords.get(i), keywords.get(j));
                    results.addAll(combinedMatches);
                }
            }
        }

        // Sắp xếp theo độ ưu tiên và giới hạn kết quả
        return results.stream()
            .sorted((a, b) -> {
                int priorityCompare = Integer.compare(b.getPriority(), a.getPriority());
                if (priorityCompare == 0) {
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                }
                return priorityCompare;
            })
            .limit(5) // Giới hạn 5 kết quả để tránh context quá dài
            .collect(Collectors.toList());
    }

    /**
     * Xây dựng context từ danh sách knowledge base
     */
    private String buildContextFromKnowledge(List<KnowledgeBase> knowledgeList) {
        if (knowledgeList.isEmpty()) {
            return "";
        }

        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("THAM KHẢO TỪ DỮ LIỆU NHÀ HÀNG:\n\n");

        for (int i = 0; i < knowledgeList.size(); i++) {
            KnowledgeBase kb = knowledgeList.get(i);
            contextBuilder.append(String.format("%d. %s (%s)\n",
                i + 1, kb.getTitle(), kb.getCategory().getDisplayName()));
            contextBuilder.append(kb.getContent());
            contextBuilder.append("\n\n");
        }

        contextBuilder.append("---\n");
        contextBuilder.append("Hãy sử dụng thông tin trên để trả lời câu hỏi của khách hàng một cách chính xác và hữu ích. ");
        contextBuilder.append("Nếu thông tin không đủ, hãy thừa nhận và đề xuất liên hệ nhân viên.\n");

        return contextBuilder.toString();
    }

    /**
     * Tìm kiếm knowledge base theo danh mục cụ thể
     */
    public List<KnowledgeBase> searchByCategory(KnowledgeBase.KnowledgeCategory category, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return knowledgeBaseRepository
                .findByCategoryAndIsActiveTrueOrderByPriorityDescCreatedAtDesc(category);
        }

        return knowledgeBaseRepository.findByCategoryAndKeyword(category, keyword.trim());
    }

    /**
     * Lấy context tổng quan về nhà hàng (để tạo system prompt)
     */
    public String getRestaurantOverviewContext() {
        try {
            List<KnowledgeBase> restaurantInfo = knowledgeBaseRepository
                .findByCategoryAndIsActiveTrueOrderByPriorityDescCreatedAtDesc(
                    KnowledgeBase.KnowledgeCategory.RESTAURANT_INFO);

            List<KnowledgeBase> operatingHours = knowledgeBaseRepository
                .findByCategoryAndIsActiveTrueOrderByPriorityDescCreatedAtDesc(
                    KnowledgeBase.KnowledgeCategory.OPERATING_HOURS);

            List<KnowledgeBase> contact = knowledgeBaseRepository
                .findByCategoryAndIsActiveTrueOrderByPriorityDescCreatedAtDesc(
                    KnowledgeBase.KnowledgeCategory.CONTACT);

            StringBuilder overview = new StringBuilder();

            if (!restaurantInfo.isEmpty()) {
                overview.append("THÔNG TIN NHÀ HÀNG:\n");
                restaurantInfo.forEach(info ->
                    overview.append("- ").append(info.getContent()).append("\n"));
                overview.append("\n");
            }

            if (!operatingHours.isEmpty()) {
                overview.append("GIỜ HOẠT ĐỘNG:\n");
                operatingHours.forEach(hours ->
                    overview.append("- ").append(hours.getContent()).append("\n"));
                overview.append("\n");
            }

            if (!contact.isEmpty()) {
                overview.append("THÔNG TIN LIÊN HỆ:\n");
                contact.forEach(contactInfo ->
                    overview.append("- ").append(contactInfo.getContent()).append("\n"));
            }

            return overview.toString();

        } catch (Exception e) {
            log.error("Lỗi khi lấy overview context: {}", e.getMessage());
            return "Nhà hàng trực tuyến chuyên phục vụ các món ăn ngon, giao hàng nhanh chóng.";
        }
    }

    /**
     * Tính điểm tương đồng giữa câu hỏi và knowledge base (đơn giản)
     */
    public double calculateSimilarity(String query, KnowledgeBase knowledge) {
        if (query == null || knowledge == null) {
            return 0.0;
        }

        String normalizedQuery = query.toLowerCase();
        String combinedKnowledge = (knowledge.getTitle() + " " +
                                  knowledge.getContent() + " " +
                                  knowledge.getKeywords()).toLowerCase();

        // Đếm số từ khóa trùng khớp (phương pháp đơn giản)
        String[] queryWords = normalizedQuery.split("\\s+");
        long matchCount = Arrays.stream(queryWords)
            .filter(word -> word.length() > 2)
            .filter(combinedKnowledge::contains)
            .count();

        return (double) matchCount / Math.max(queryWords.length, 1);
    }
}
