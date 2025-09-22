package com.foodorder.backend.chatbot.dto;

import com.foodorder.backend.chatbot.entity.KnowledgeBase.KnowledgeCategory;
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
public class KnowledgeBaseDTO {

    private Long id;

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    @NotBlank(message = "Nội dung không được để trống")
    private String content;

    private String keywords;

    @NotNull(message = "Danh mục không được để trống")
    private KnowledgeCategory category;

    @Min(value = 1, message = "Độ ưu tiên phải từ 1 đến 10")
    @Max(value = 10, message = "Độ ưu tiên phải từ 1 đến 10")
    private Integer priority;

    private Boolean isActive;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Long createdBy;
}
