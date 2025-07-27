package com.foodorder.backend.points.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PointsDTO {
    private int amount;
}

// Đã chuyển sang PointsResponseDTO ở thư mục response, không dùng chung tên DTO
// cho request/response.
