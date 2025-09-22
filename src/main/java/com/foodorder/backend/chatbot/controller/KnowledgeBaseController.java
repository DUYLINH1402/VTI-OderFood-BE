package com.foodorder.backend.chatbot.controller;

import com.foodorder.backend.chatbot.dto.KnowledgeBaseDTO;
import com.foodorder.backend.chatbot.entity.KnowledgeBase;
import com.foodorder.backend.chatbot.service.KnowledgeBaseService;
import com.foodorder.backend.exception.ApiError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * Controller quản lý Knowledge Base cho Admin
 */
@RestController
@RequestMapping("/api/admin/knowledge-base")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    /**
     * Tạo mới knowledge base
     */
    @PostMapping
    public ResponseEntity<?> createKnowledgeBase(@Valid @RequestBody KnowledgeBaseDTO dto) {
        try {
            // TODO: Lấy user ID từ authentication
            Long createdBy = 1L; // Tạm thời hardcode

            KnowledgeBaseDTO created = knowledgeBaseService.createKnowledgeBase(dto, createdBy);
            log.info("Admin tạo knowledge base mới: {}", created.getTitle());

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Tạo knowledge base thành công",
                "data", created
            ));

        } catch (Exception e) {
            log.error("Lỗi khi tạo knowledge base: {}", e.getMessage());
            ApiError apiError = ApiError.builder()
                .errorCode("KNOWLEDGE_CREATE_FAILED")
                .message("Không thể tạo knowledge base")
                .details(e.getMessage())
                .build();
            return ResponseEntity.badRequest().body(apiError);
        }
    }

    /**
     * Cập nhật knowledge base
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateKnowledgeBase(@PathVariable Long id,
                                               @Valid @RequestBody KnowledgeBaseDTO dto) {
        try {
            // TODO: Lấy user ID từ authentication
            Long updatedBy = 1L; // Tạm thời hardcode

            KnowledgeBaseDTO updated = knowledgeBaseService.updateKnowledgeBase(id, dto, updatedBy);
            log.info("Admin cập nhật knowledge base ID: {}", id);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cập nhật knowledge base thành công",
                "data", updated
            ));

        } catch (Exception e) {
            log.error("Lỗi khi cập nhật knowledge base: {}", e.getMessage());
            ApiError apiError = ApiError.builder()
                .errorCode("KNOWLEDGE_UPDATE_FAILED")
                .message("Không thể cập nhật knowledge base")
                .details(e.getMessage())
                .build();
            return ResponseEntity.badRequest().body(apiError);
        }
    }

    /**
     * Lấy danh sách knowledge base với phân trang
     */
    @GetMapping
    public ResponseEntity<?> getKnowledgeBaseList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword) {
        try {
            Pageable pageable = PageRequest.of(page, size);

            if (keyword != null && !keyword.trim().isEmpty()) {
                // Tìm kiếm theo từ khóa
                List<KnowledgeBaseDTO> results = knowledgeBaseService.searchKnowledgeBase(keyword);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", results,
                    "total", results.size()
                ));
            } else if (category != null && !category.trim().isEmpty()) {
                // Lọc theo danh mục
                try {
                    KnowledgeBase.KnowledgeCategory categoryEnum =
                        KnowledgeBase.KnowledgeCategory.valueOf(category.toUpperCase());
                    List<KnowledgeBaseDTO> results =
                        knowledgeBaseService.getKnowledgeBaseByCategory(categoryEnum);
                    return ResponseEntity.ok(Map.of(
                        "success", true,
                        "data", results,
                        "total", results.size()
                    ));
                } catch (IllegalArgumentException e) {
                    ApiError apiError = ApiError.builder()
                        .errorCode("INVALID_CATEGORY")
                        .message("Danh mục không hợp lệ")
                        .details("Danh mục phải là một trong: " +
                               String.join(", ", getCategoryNames()))
                        .build();
                    return ResponseEntity.badRequest().body(apiError);
                }
            } else {
                // Lấy tất cả với phân trang
                Page<KnowledgeBaseDTO> results = knowledgeBaseService.getKnowledgeBasePage(pageable);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", results.getContent(),
                    "total", results.getTotalElements(),
                    "totalPages", results.getTotalPages(),
                    "currentPage", page
                ));
            }

        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách knowledge base: {}", e.getMessage());
            ApiError apiError = ApiError.builder()
                .errorCode("KNOWLEDGE_LIST_FAILED")
                .message("Không thể lấy danh sách knowledge base")
                .details(e.getMessage())
                .build();
            return ResponseEntity.internalServerError().body(apiError);
        }
    }

    /**
     * Lấy chi tiết knowledge base
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getKnowledgeBase(@PathVariable Long id) {
        try {
            KnowledgeBaseDTO dto = knowledgeBaseService.getKnowledgeBaseById(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", dto
            ));

        } catch (Exception e) {
            log.error("Lỗi khi lấy knowledge base ID {}: {}", id, e.getMessage());
            ApiError apiError = ApiError.builder()
                .errorCode("KNOWLEDGE_NOT_FOUND")
                .message("Không tìm thấy knowledge base")
                .details(e.getMessage())
                .build();
            return ResponseEntity.badRequest().body(apiError);
        }
    }

    /**
     * Xóa knowledge base (soft delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteKnowledgeBase(@PathVariable Long id) {
        try {
            boolean success = knowledgeBaseService.deleteKnowledgeBase(id);

            if (success) {
                log.info("Admin xóa knowledge base ID: {}", id);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Xóa knowledge base thành công"
                ));
            } else {
                ApiError apiError = ApiError.builder()
                    .errorCode("KNOWLEDGE_NOT_FOUND")
                    .message("Không tìm thấy knowledge base để xóa")
                    .build();
                return ResponseEntity.badRequest().body(apiError);
            }

        } catch (Exception e) {
            log.error("Lỗi khi xóa knowledge base ID {}: {}", id, e.getMessage());
            ApiError apiError = ApiError.builder()
                .errorCode("KNOWLEDGE_DELETE_FAILED")
                .message("Không thể xóa knowledge base")
                .details(e.getMessage())
                .build();
            return ResponseEntity.internalServerError().body(apiError);
        }
    }

    /**
     * Khởi tạo dữ liệu mẫu
     */
    @PostMapping("/initialize-sample")
    public ResponseEntity<?> initializeSampleData() {
        try {
            knowledgeBaseService.initializeSampleData();
            log.info("Admin khởi tạo dữ liệu mẫu cho knowledge base");

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Khởi tạo dữ liệu mẫu thành công"
            ));

        } catch (Exception e) {
            log.error("Lỗi khi khởi tạo dữ liệu mẫu: {}", e.getMessage());
            ApiError apiError = ApiError.builder()
                .errorCode("SAMPLE_DATA_INIT_FAILED")
                .message("Không thể khởi tạo dữ liệu mẫu")
                .details(e.getMessage())
                .build();
            return ResponseEntity.internalServerError().body(apiError);
        }
    }

    /**
     * Lấy danh sách các danh mục có sẵn
     */
    @GetMapping("/categories")
    public ResponseEntity<?> getCategories() {
        try {
            List<Map<String, String>> categories = List.of(
                Map.of("value", "RESTAURANT_INFO", "label", "Thông tin nhà hàng"),
                Map.of("value", "MENU_INFO", "label", "Thông tin thực đơn"),
                Map.of("value", "ORDER_POLICY", "label", "Chính sách đặt hàng"),
                Map.of("value", "PAYMENT_INFO", "label", "Thông tin thanh toán"),
                Map.of("value", "DELIVERY_INFO", "label", "Thông tin giao hàng"),
                Map.of("value", "PROMOTION", "label", "Khuyến mãi"),
                Map.of("value", "FAQ", "label", "Câu hỏi thường gặp"),
                Map.of("value", "CONTACT", "label", "Thông tin liên hệ"),
                Map.of("value", "OPERATING_HOURS", "label", "Giờ hoạt động"),
                Map.of("value", "OTHER", "label", "Khác")
            );

            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", categories
            ));

        } catch (Exception e) {
            log.error("Lỗi khi lấy danh sách categories: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                Map.of("success", false, "message", "Lỗi server")
            );
        }
    }

    /**
     * Lấy tên các danh mục
     */
    private String[] getCategoryNames() {
        return new String[]{
            "RESTAURANT_INFO", "MENU_INFO", "ORDER_POLICY", "PAYMENT_INFO",
            "DELIVERY_INFO", "PROMOTION", "FAQ", "CONTACT", "OPERATING_HOURS", "OTHER"
        };
    }
}
