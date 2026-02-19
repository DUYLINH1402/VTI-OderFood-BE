package com.foodorder.backend.search.controller;

import com.foodorder.backend.search.dto.FoodSearchDTO;
import com.foodorder.backend.search.dto.FoodSearchResponse;
import com.foodorder.backend.search.service.AlgoliaSearchService;
import com.foodorder.backend.security.annotation.RequireSuperAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller xử lý tìm kiếm món ăn qua Algolia
 *
 * API endpoint: GET /api/v1/search
 * Tìm kiếm full-text với hiệu suất cao thông qua Algolia Search
 */
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Food Search", description = "API tìm kiếm món ăn qua Algolia")
public class SearchController {

    private final AlgoliaSearchService algoliaSearchService;

    /**
     * Tìm kiếm món ăn với từ khóa
     *
     * @param query Từ khóa tìm kiếm (required)
     * @param page Số trang, mặc định 0
     * @param hitsPerPage Số kết quả mỗi trang, mặc định 10
     * @return Danh sách món ăn phù hợp
     */
    @GetMapping
    @Operation(
            summary = "Tìm kiếm món ăn",
            description = "Tìm kiếm món ăn theo từ khóa sử dụng Algolia full-text search. " +
                    "Hỗ trợ tìm kiếm theo tên và mô tả món ăn."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tìm kiếm thành công"),
            @ApiResponse(responseCode = "400", description = "Tham số không hợp lệ")
    })
    public ResponseEntity<FoodSearchResponse> search(
            @Parameter(description = "Từ khóa tìm kiếm", required = true, example = "phở bò")
            @RequestParam String query,

            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Số kết quả mỗi trang", example = "10")
            @RequestParam(defaultValue = "10") int hitsPerPage
    ) {
        log.info("Tìm kiếm món ăn với query: '{}', page: {}, hitsPerPage: {}", query, page, hitsPerPage);

        List<FoodSearchDTO> results = algoliaSearchService.search(query, page, hitsPerPage);

        FoodSearchResponse response = FoodSearchResponse.builder()
                .results(results)
                .query(query)
                .totalResults(results.size())
                .page(page)
                .hitsPerPage(hitsPerPage)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint để reindex toàn bộ dữ liệu (chỉ dành cho Super Admin)
     * Sử dụng khi cần rebuild index hoặc khởi tạo ban đầu
     */
    @PostMapping("/reindex")
    @RequireSuperAdmin
    @Operation(
            summary = "Reindex toàn bộ món ăn",
            description = "Đồng bộ lại toàn bộ dữ liệu món ăn từ MySQL lên Algolia. " +
                    "Chỉ SUPER_ADMIN mới có quyền thực hiện."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bắt đầu reindex thành công"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập - Yêu cầu SUPER_ADMIN")
    })
    public ResponseEntity<String> reindexAll() {
        log.info("SUPER_ADMIN bắt đầu reindex toàn bộ món ăn lên Algolia");
        algoliaSearchService.reindexAll();
        return ResponseEntity.ok("Đã bắt đầu reindex toàn bộ món ăn. Vui lòng kiểm tra log để theo dõi tiến trình.");
    }

    /**
     * Endpoint để đẩy dữ liệu mẫu ban đầu lên Algolia (chỉ dành cho Super Admin)
     * Sử dụng khi setup hệ thống lần đầu hoặc cần khởi tạo lại index
     */
    @PostMapping("/init")
    @RequireSuperAdmin
    @Operation(
            summary = "Khởi tạo dữ liệu Algolia",
            description = "Đẩy toàn bộ dữ liệu món ăn từ MySQL lên Algolia lần đầu tiên. " +
                    "API này sẽ xóa toàn bộ dữ liệu cũ trên Algolia (nếu có) và đẩy lại từ đầu. " +
                    "Chỉ SUPER_ADMIN mới có quyền thực hiện."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Khởi tạo thành công"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập - Yêu cầu SUPER_ADMIN")
    })
    public ResponseEntity<Map<String, Object>> initAlgoliaData() {
        log.info("SUPER_ADMIN bắt đầu khởi tạo dữ liệu Algolia");

        int syncedCount = algoliaSearchService.initializeAlgoliaIndex();

        Map<String, Object> response = Map.of(
                "success", true,
                "message", "Khởi tạo dữ liệu Algolia thành công",
                "syncedFoods", syncedCount
        );

        log.info("Hoàn tất khởi tạo Algolia với {} món ăn", syncedCount);
        return ResponseEntity.ok(response);
    }
}

