package com.foodorder.backend.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO cho việc cập nhật ghi chú nội bộ (internal_note)
 * Dùng cho đối soát, lưu ý về dòng tiền hoặc khách hàng mà chỉ nội bộ quản trị thấy
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body để cập nhật ghi chú nội bộ của đơn hàng")
public class UpdateInternalNoteRequest {

    @Schema(
        description = "Ghi chú nội bộ (chỉ Admin/Staff thấy, dùng cho đối soát, lưu ý về dòng tiền hoặc khách hàng)",
        example = "Khách hàng VIP, cần ưu tiên giao hàng"
    )
    @Size(max = 2000, message = "Ghi chú nội bộ không được vượt quá 2000 ký tự")
    private String internalNote;
}

