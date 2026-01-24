package com.foodorder.backend.points.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO cho thông tin điểm thưởng")
public class PointsDTO {

    @Schema(description = "Số điểm", example = "100")
    private int amount;
}

