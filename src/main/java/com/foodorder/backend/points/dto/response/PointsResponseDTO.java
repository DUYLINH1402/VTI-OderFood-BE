package com.foodorder.backend.points.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response chứa thông tin điểm thưởng khả dụng")
public class PointsResponseDTO {

    @Schema(description = "Số điểm khả dụng hiện tại", example = "500")
    private int availablePoints;
}
