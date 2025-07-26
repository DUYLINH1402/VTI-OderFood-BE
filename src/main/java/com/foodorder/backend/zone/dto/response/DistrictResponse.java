package com.foodorder.backend.zone.dto.response;

import com.foodorder.backend.zone.entity.District;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistrictResponse {
    private Long id;
    private String name;
    private BigDecimal deliveryFee;

    public static DistrictResponse fromEntity(District district) {
        return new DistrictResponse(
                district.getId(),
                district.getName(),
                district.getDeliveryFee()
        );
    }


}



