package com.foodorder.backend.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * Response wrapper cho kết quả tìm kiếm từ Algolia
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response chứa kết quả tìm kiếm món ăn từ Algolia")
public class FoodSearchResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "Danh sách món ăn tìm được")
    private List<FoodSearchDTO> results;

    @Schema(description = "Từ khóa tìm kiếm", example = "phở bò")
    private String query;

    @Schema(description = "Tổng số kết quả", example = "15")
    private int totalResults;

    @Schema(description = "Số trang hiện tại (bắt đầu từ 0)", example = "0")
    private int page;

    @Schema(description = "Số kết quả mỗi trang", example = "10")
    private int hitsPerPage;
}

