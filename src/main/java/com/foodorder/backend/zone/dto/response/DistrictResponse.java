package com.foodorder.backend.zone.dto.response;

import com.foodorder.backend.zone.entity.District;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response chứa thông tin quận/huyện")
public class DistrictResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID của quận/huyện", example = "1")
    private Long id;

    @Schema(description = "Tên quận/huyện", example = "Quận 1")
    private String name;

    @Schema(description = "Phí giao hàng cho quận/huyện này (VND)", example = "15000")
    private BigDecimal deliveryFee;

    public static DistrictResponse fromEntity(District district) {
        return new DistrictResponse(
                district.getId(),
                district.getName(),
                district.getDeliveryFee()
        );
    }


}



