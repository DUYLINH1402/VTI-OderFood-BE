package com.foodorder.backend.search.service.impl;

import com.algolia.api.SearchClient;
import com.algolia.model.search.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodorder.backend.food.entity.Food;
import com.foodorder.backend.food.repository.FoodRepository;
import com.foodorder.backend.search.dto.FoodSearchDTO;
import com.foodorder.backend.search.service.AlgoliaSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation của AlgoliaSearchService
 *
 * Service này xử lý việc đồng bộ dữ liệu món ăn giữa MySQL và Algolia
 * Các thao tác write được thực hiện async để không block main thread
 */
@Service
@Slf4j
public class AlgoliaSearchServiceImpl implements AlgoliaSearchService {

    private final SearchClient searchClient;
    private final String indexName;
    private final FoodRepository foodRepository;
    private final ObjectMapper objectMapper;

    public AlgoliaSearchServiceImpl(
            SearchClient searchClient,
            @Value("${algolia.index-name}") String indexName,
            FoodRepository foodRepository,
            ObjectMapper objectMapper) {
        this.searchClient = searchClient;
        this.indexName = indexName;
        this.foodRepository = foodRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Chuyển đổi Food entity sang FoodSearchDTO
     */
    private FoodSearchDTO convertToDTO(Food food) {
        return FoodSearchDTO.builder()
                .objectID(String.valueOf(food.getId()))
                .name(food.getName())
                .description(food.getDescription())
                .price(food.getPrice() != null ? food.getPrice().doubleValue() : null)
                .imageUrl(food.getImageUrl())
                .slug(food.getSlug())
                .categoryName(food.getCategory() != null ? food.getCategory().getName() : null)
                .categoryId(food.getCategory() != null ? food.getCategory().getId() : null)
                .status(food.getStatus() != null ? food.getStatus().name() : null)
                .isBestSeller(food.getIsBestSeller())
                .isNew(food.getIsNew())
                .isFeatured(food.getIsFeatured())
                .build();
    }

    /**
     * Chuyển đổi DTO sang Map để Algolia xử lý
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> convertToMap(FoodSearchDTO dto) {
        return objectMapper.convertValue(dto, Map.class);
    }

    @Override
    @Async
    public void syncToAlgolia(Food food) {
        try {
            // Chỉ đồng bộ món ăn đang hoạt động
            if (food.getIsActive() == null || !food.getIsActive()) {
                log.info("Bỏ qua đồng bộ món ăn không hoạt động: {}", food.getId());
                removeFromAlgolia(food.getId());
                return;
            }

            FoodSearchDTO dto = convertToDTO(food);
            Map<String, Object> record = convertToMap(dto);

            searchClient.saveObject(indexName, record);
            log.info("Đồng bộ món ăn {} lên Algolia thành công", food.getId());

        } catch (Exception e) {
            log.error("Lỗi khi đồng bộ món ăn {} lên Algolia: {}", food.getId(), e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void syncBatchToAlgolia(List<Food> foods) {
        try {
            // Lọc chỉ lấy món ăn đang hoạt động
            List<Map<String, Object>> records = foods.stream()
                    .filter(food -> food.getIsActive() != null && food.getIsActive())
                    .map(this::convertToDTO)
                    .map(this::convertToMap)
                    .collect(Collectors.toList());

            if (records.isEmpty()) {
                log.info("Không có món ăn nào cần đồng bộ");
                return;
            }

            searchClient.saveObjects(indexName, records);
            log.info("Đồng bộ batch {} món ăn lên Algolia thành công", records.size());

        } catch (Exception e) {
            log.error("Lỗi khi đồng bộ batch món ăn lên Algolia: {}", e.getMessage(), e);
        }
    }

    @Override
    @Async
    public void removeFromAlgolia(Long foodId) {
        try {
            searchClient.deleteObject(indexName, String.valueOf(foodId));
            log.info("Xóa món ăn {} khỏi Algolia thành công", foodId);

        } catch (Exception e) {
            log.error("Lỗi khi xóa món ăn {} khỏi Algolia: {}", foodId, e.getMessage(), e);
        }
    }

    @Override
    public List<FoodSearchDTO> search(String query, int page, int hitsPerPage) {
        try {
            // Tạo search request cho Algolia v4
            SearchForHits searchForHits = new SearchForHits()
                    .setIndexName(indexName)
                    .setQuery(query)
                    .setPage(page)
                    .setHitsPerPage(hitsPerPage)
                    .setFilters("status:AVAILABLE");

            // Thực hiện tìm kiếm - sử dụng SearchMethodParams
            SearchMethodParams searchMethodParams = new SearchMethodParams()
                    .setRequests(List.of(searchForHits));

            SearchResponses<FoodSearchDTO> responses = searchClient.search(searchMethodParams, FoodSearchDTO.class);

            // Lấy kết quả từ response đầu tiên
            if (!responses.getResults().isEmpty()) {
                SearchResult<FoodSearchDTO> result = responses.getResults().get(0);
                if (result instanceof SearchResponse<FoodSearchDTO> searchResponse) {
                    List<FoodSearchDTO> results = searchResponse.getHits();
                    log.info("Tìm kiếm Algolia với query '{}': {} kết quả", query, results.size());
                    return results;
                }
            }

            log.info("Tìm kiếm Algolia với query '{}': 0 kết quả", query);
            return List.of();

        } catch (Exception e) {
            log.error("Lỗi khi tìm kiếm Algolia với query '{}': {}", query, e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    @Async
    public void reindexAll() {
        try {
            log.info("Bắt đầu reindex toàn bộ món ăn...");

            // Xóa toàn bộ index cũ
            searchClient.clearObjects(indexName);

            // Lấy tất cả món ăn đang hoạt động từ MySQL
            List<Food> allFoods = foodRepository.findAllByIsActiveTrue();

            if (allFoods.isEmpty()) {
                log.info("Không có món ăn nào để reindex");
                return;
            }

            // Đồng bộ batch
            syncBatchToAlgolia(allFoods);

            log.info("Hoàn tất reindex {} món ăn lên Algolia", allFoods.size());

        } catch (Exception e) {
            log.error("Lỗi khi reindex Algolia: {}", e.getMessage(), e);
        }
    }

    @Override
    public int initializeAlgoliaIndex() {
        try {
            log.info("SUPER_ADMIN: Bắt đầu khởi tạo dữ liệu Algolia...");

            // Xóa toàn bộ index cũ (nếu có)
            searchClient.clearObjects(indexName);
            log.info("Đã xóa toàn bộ dữ liệu cũ trên Algolia index: {}", indexName);

            // Lấy tất cả món ăn đang hoạt động từ MySQL
            List<Food> allFoods = foodRepository.findAllByIsActiveTrue();

            if (allFoods.isEmpty()) {
                log.info("Không có món ăn nào trong database để đồng bộ");
                return 0;
            }

            // Chuyển đổi sang DTO và Map
            List<Map<String, Object>> records = allFoods.stream()
                    .map(this::convertToDTO)
                    .map(this::convertToMap)
                    .collect(Collectors.toList());

            // Đẩy dữ liệu lên Algolia (synchronous)
            searchClient.saveObjects(indexName, records);

            log.info("SUPER_ADMIN: Hoàn tất khởi tạo Algolia với {} món ăn", records.size());
            return records.size();

        } catch (Exception e) {
            log.error("SUPER_ADMIN: Lỗi khi khởi tạo Algolia: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi khởi tạo dữ liệu Algolia: " + e.getMessage(), e);
        }
    }
}

