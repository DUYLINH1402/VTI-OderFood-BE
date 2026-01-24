package com.foodorder.backend.share.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response trả về kết quả share")
public class ShareResponse {

    @Schema(description = "Tổng số lượt share của đối tượng", example = "50")
    private long totalShares;

    @Schema(description = "Loại đối tượng", example = "FOOD")
    private String targetType;

    @Schema(description = "ID của đối tượng", example = "1")
    private Long targetId;

    @Schema(description = "Nền tảng đã chia sẻ", example = "FACEBOOK")
    private String platform;

    @Schema(description = "Thông báo kết quả", example = "Chia sẻ thành công")
    private String message;
}

