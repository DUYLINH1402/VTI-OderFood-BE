package com.foodorder.backend.chatbot.dto;

import com.foodorder.backend.chatbot.entity.KnowledgeBase.KnowledgeCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.LocalDateTime;

/**
 * DTO cho việc tạo và cập nhật Knowledge Base
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO để tạo/cập nhật Knowledge Base cho chatbot")
public class KnowledgeBaseDTO {

    @Schema(description = "ID của knowledge base", example = "1")
    private Long id;

    @Schema(
        description = "Tiêu đề của knowledge base",
        example = "Hướng dẫn đặt hàng",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @Schema(
        description = "Nội dung chi tiết của knowledge base",
        example = "Để đặt hàng, bạn cần chọn món ăn, thêm vào giỏ hàng và tiến hành thanh toán...",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank(message = "Nội dung không được để trống")
    private String content;

    @Schema(
        description = "Các từ khóa liên quan (phân cách bằng dấu phẩy)",
        example = "đặt hàng, order, mua hàng, thanh toán"
    )
    private String keywords;

    @Schema(
        description = "Danh mục của knowledge base",
        example = "FAQ",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "Danh mục không được để trống")
    private KnowledgeCategory category;

    @Schema(
        description = "Độ ưu tiên hiển thị (1-10, số nhỏ ưu tiên cao)",
        example = "1",
        minimum = "1",
        maximum = "10"
    )
    @Min(value = 1, message = "Độ ưu tiên phải từ 1 đến 10")
    @Max(value = 10, message = "Độ ưu tiên phải từ 1 đến 10")
    private Integer priority;

    @Schema(description = "Trạng thái hoạt động", example = "true")
    private Boolean isActive;

    @Schema(description = "Thời gian tạo", example = "2025-01-20T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Thời gian cập nhật gần nhất", example = "2025-01-20T15:45:00")
    private LocalDateTime updatedAt;

    @Schema(description = "ID của người tạo", example = "1")
    private Long createdBy;
}
