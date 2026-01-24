package com.foodorder.backend.share.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request để ghi nhận lượt share")
public class ShareRequest {

    @NotNull(message = "Target type là bắt buộc")
    @Schema(description = "Loại đối tượng được share", example = "FOOD", allowableValues = {"FOOD", "BLOG"})
    private String targetType;

    @NotNull(message = "Target ID là bắt buộc")
    @Schema(description = "ID của đối tượng được share", example = "1")
    private Long targetId;

    @NotNull(message = "Platform là bắt buộc")
    @Schema(description = "Nền tảng chia sẻ", example = "FACEBOOK", allowableValues = {"FACEBOOK", "ZALO"})
    private String platform;
}

