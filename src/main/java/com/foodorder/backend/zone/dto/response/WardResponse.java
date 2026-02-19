package com.foodorder.backend.zone.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response chứa thông tin phường/xã")
public class WardResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID của phường/xã", example = "1")
    private Long id;

    @Schema(description = "Tên phường/xã", example = "Phường Bến Nghé")
    private String name;

    @Schema(description = "ID của quận/huyện chứa phường/xã này", example = "1")
    private Long districtId;
}
