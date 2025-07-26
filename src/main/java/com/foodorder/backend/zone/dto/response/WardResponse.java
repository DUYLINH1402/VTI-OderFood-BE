package com.foodorder.backend.zone.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WardResponse {
    private Long id;
    private String name;
    private Long districtId;
}
