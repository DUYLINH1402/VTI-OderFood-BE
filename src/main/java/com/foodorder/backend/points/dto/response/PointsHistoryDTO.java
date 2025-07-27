package com.foodorder.backend.points.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointsHistoryDTO {
    private Long id;
    private String type;
    private Integer amount;
    private Long orderId;
    private String description;
    private LocalDateTime createdAt;
    private Integer totalPointsAfter;
}
