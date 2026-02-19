package com.foodorder.backend.search.service;

import com.foodorder.backend.food.entity.Food;
import com.foodorder.backend.search.dto.FoodSearchDTO;

import java.util.List;

/**
 * Service xử lý đồng bộ dữ liệu với Algolia Search
 *
 * Chức năng chính:
 * - Đồng bộ món ăn khi thêm/sửa
 * - Xóa món ăn khỏi Algolia khi xóa trong MySQL
 * - Tìm kiếm món ăn qua Algolia
 */
public interface AlgoliaSearchService {

    /**
     * Đồng bộ một món ăn lên Algolia
     * Gọi khi thêm mới hoặc cập nhật món ăn
     *
     * @param food Entity Food cần đồng bộ
     */
    void syncToAlgolia(Food food);

    /**
     * Đồng bộ nhiều món ăn lên Algolia (batch)
     * Sử dụng khi cần sync hàng loạt (ví dụ: initial sync)
     *
     * @param foods Danh sách Food entities cần đồng bộ
     */
    void syncBatchToAlgolia(List<Food> foods);

    /**
     * Xóa món ăn khỏi Algolia
     * Gọi khi xóa món ăn khỏi MySQL
     *
     * @param foodId ID của món ăn cần xóa
     */
    void removeFromAlgolia(Long foodId);

    /**
     * Tìm kiếm món ăn qua Algolia
     *
     * @param query Từ khóa tìm kiếm
     * @param page Số trang (bắt đầu từ 0)
     * @param hitsPerPage Số kết quả mỗi trang
     * @return Danh sách FoodSearchDTO phù hợp
     */
    List<FoodSearchDTO> search(String query, int page, int hitsPerPage);

    /**
     * Đồng bộ toàn bộ món ăn từ MySQL lên Algolia
     * Sử dụng khi cần rebuild index hoặc khởi tạo ban đầu
     */
    void reindexAll();

    /**
     * Khởi tạo dữ liệu Algolia lần đầu (synchronous)
     * Xóa toàn bộ dữ liệu cũ và đẩy lại từ MySQL
     *
     * @return Số lượng món ăn đã được đồng bộ
     */
    int initializeAlgoliaIndex();
}

