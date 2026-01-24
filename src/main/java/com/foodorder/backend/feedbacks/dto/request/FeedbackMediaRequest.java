package com.foodorder.backend.feedbacks.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request body cho media đính kèm feedback (hình ảnh/video)")
public class FeedbackMediaRequest {

    @Schema(
        description = "Loại media",
        example = "IMAGE",
        allowableValues = {"IMAGE", "VIDEO"},
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String type;

    @Schema(
        description = "URL của media",
        example = "https://example.com/feedback/image1.jpg",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String mediaUrl;

    @Schema(
        description = "URL hình thumbnail (dùng cho video)",
        example = "https://example.com/feedback/thumb1.jpg"
    )
    private String thumbnailUrl;

    @Schema(
        description = "Thứ tự hiển thị",
        example = "1"
    )
    private Integer displayOrder;
}
