package com.foodorder.backend.feedbacks.dto.reponse;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;


@Getter
@Setter
@Schema(description = "Response chứa thông tin media đính kèm feedback")
public class FeedbackMediaResponse {

    @Schema(description = "ID của media", example = "1")
    private Long id;

    @Schema(description = "Loại media", example = "IMAGE", allowableValues = {"IMAGE", "VIDEO"})
    private String type;

    @Schema(description = "URL của media", example = "https://example.com/feedback/image1.jpg")
    private String mediaUrl;

    @Schema(description = "URL hình thumbnail", example = "https://example.com/feedback/thumb1.jpg")
    private String thumbnailUrl;

    @Schema(description = "Thứ tự hiển thị", example = "1")
    private Integer displayOrder;

    @Schema(description = "Thời gian tạo", example = "2025-01-20 10:30:00")
    private Timestamp createdAt;
}
