package com.foodorder.backend.like.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request để like/unlike một đối tượng")
public class LikeRequest {

    @NotNull(message = "Target type là bắt buộc")
    @Schema(description = "Loại đối tượng cần like", example = "FOOD", allowableValues = {"FOOD", "BLOG", "MOVIE"})
    private String targetType;

    @NotNull(message = "Target ID là bắt buộc")
    @Schema(description = "ID của đối tượng cần like", example = "1")
    private Long targetId;
}

